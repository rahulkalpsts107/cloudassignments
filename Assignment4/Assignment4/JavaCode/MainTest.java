package com.aws.assignment3;
import java.util.ArrayList;
import java.util.Collection;
/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.SetLoadBalancerPoliciesOfListenerRequest;
import com.aws.assignment3.util.*;
import com.amazonaws.services.elasticloadbalancing.model.Listener;

public class MainTest {
	public static AmazonEC2 ec2 = null;

    public static List<Instance> listActiveInstances() throws AmazonClientException {
        ec2.setRegion(Region.getRegion(Regions.US_WEST_2));
    	List<Instance> instances = new ArrayList<Instance>();
    	List<String> serviceInstanceIds = new ArrayList<String>();
    	serviceInstanceIds.add(Constants.INSTANCE1);
    	serviceInstanceIds.add(Constants.INSTANCE2);
    	serviceInstanceIds.add(Constants.INSTANCE3);
    	DescribeInstancesRequest request = new DescribeInstancesRequest();
    	request.setInstanceIds(serviceInstanceIds);
           DescribeInstancesResult response = ec2.describeInstances(request);

    	List<Reservation> reservations = response.getReservations();
    	System.out.println("Reservations found "+reservations.size());
    	for (Reservation reservation : reservations) {
    		System.out.println("Reservation Id "+reservation.getReservationId());
    		for (Instance instance : reservation.getInstances()) {
    			instances.add(instance);
    			System.out.println(instance.getInstanceId());
    			System.out.println(instance.getImageId());
    			System.out.println(instance.getPublicIpAddress());
    			System.out.println(instance.getState().getName());
    			System.out.println("***********************");
    		}
    	}

    	return instances;
    }

    public static void createScalingPolicyAndAlarm(AmazonAutoScalingClient client)
    {
    	PutScalingPolicyRequest scaleUpPolicy = new PutScalingPolicyRequest();
    	scaleUpPolicy.setAutoScalingGroupName(Constants.AUTOSCALE_GROUP);
    	scaleUpPolicy.setAdjustmentType("ChangeInCapacity");
    	scaleUpPolicy.setCooldown(200);
    	scaleUpPolicy.setScalingAdjustment(2);
    	scaleUpPolicy.setPolicyName("ScaleUpPolicy");
    	
    	PutScalingPolicyRequest scaleDownPolicy = new PutScalingPolicyRequest();
    	scaleDownPolicy.setAutoScalingGroupName(Constants.AUTOSCALE_GROUP);
    	scaleDownPolicy.setAdjustmentType("ChangeInCapacity");
    	scaleDownPolicy.setCooldown(200);
    	scaleDownPolicy.setPolicyName("ScaleDownPolicy");
    	scaleDownPolicy.setScalingAdjustment(-2);
    	
    	//client.attachLoadBalancers();    	
    	client.configureRegion(Regions.US_WEST_2);
    	String policy1Arn = client.putScalingPolicy(scaleUpPolicy).getPolicyARN();
    	client.putScalingPolicy(scaleDownPolicy);
    	String policy2Arn = client.putScalingPolicy(scaleDownPolicy).getPolicyARN();
    	System.out.println("ARN 1 = "+policy1Arn);
    	System.out.println("ARN 2 = "+policy2Arn);
    	
    	System.out.println("Policies added");
    	PutMetricAlarmRequest upRequest = new PutMetricAlarmRequest();
    	List<Dimension> dimensions = new ArrayList();
        Dimension dimension = new Dimension();
        dimension.setName("AutoScalingGroupName");
        dimension.setValue(Constants.AUTOSCALE_GROUP);
        dimensions.add(dimension);
        List<String> actions = new ArrayList();
        actions.add(policy1Arn);
        
        upRequest.setAlarmActions(actions);
        upRequest.setAlarmName("alarm-scale-up");
    	upRequest.setMetricName("CPUUtilization");
    	upRequest.setUnit("Percent");
        upRequest.setDimensions(dimensions);
        upRequest.setNamespace("AWS/EC2");
        upRequest.setStatistic(Statistic.Average);
        upRequest.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
        upRequest.setThreshold(40d);
        upRequest.setPeriod(60);
        upRequest.setEvaluationPeriods(2);
        
        PutMetricAlarmRequest downRequest = new PutMetricAlarmRequest();
    	List<Dimension> dimensions1 = new ArrayList();
        Dimension dimension1 = new Dimension();
        dimension1.setName("AutoScalingGroupName");
        dimension1.setValue(Constants.AUTOSCALE_GROUP);
        dimensions1.add(dimension1);
        List<String> actions1 = new ArrayList();
        actions.add(policy2Arn);
        
        downRequest.setAlarmActions(actions);
        downRequest.setAlarmName("alarm-scale-down");
        downRequest.setMetricName("CPUUtilization");
        downRequest.setUnit("Percent");
        downRequest.setDimensions(dimensions1);
        downRequest.setNamespace("AWS/EC2");
        downRequest.setStatistic(Statistic.Average);
        downRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
        downRequest.setThreshold(40d);
        downRequest.setPeriod(60);
        downRequest.setEvaluationPeriods(5);
        
        AmazonCloudWatchClient awsCloudWatch = new AmazonCloudWatchClient(new DefaultAWSCredentialsProviderChain().getCredentials());
        awsCloudWatch.withRegion(Regions.US_WEST_2);
        awsCloudWatch.putMetricAlarm(upRequest);
        awsCloudWatch.putMetricAlarm(downRequest);
        System.out.println("Metrics added");
    	
    }
    
    public static CreateLaunchConfigurationRequest createAutoScaleConfig() throws InterruptedException
    {
    	CreateLaunchConfigurationRequest launchConfig = new CreateLaunchConfigurationRequest();
    	launchConfig.setImageId(Constants.IMAGE_ID);
    	launchConfig.setInstanceType(Constants.INSTANCE_TYPE);
    	launchConfig.setLaunchConfigurationName("MyLaunchConfig");
    	launchConfig.withKeyName("rahulramesh-keypair-oregon");
    	
    	/*Create AutoScale Config*/
    	AmazonAutoScalingClient client = new AmazonAutoScalingClient(new DefaultAWSCredentialsProviderChain().getCredentials());
    	//client.attachLoadBalancers();
    	client.configureRegion(Regions.US_WEST_2);
    	client.createLaunchConfiguration(launchConfig);
    	
    	return launchConfig;
    }
    
    public static void createAutoscaleGroup(List<Instance> instances, CreateLaunchConfigurationRequest launchConfig)
    {
    	CreateAutoScalingGroupRequest autoScale = new CreateAutoScalingGroupRequest();
    	List<String> balancers = new ArrayList<>();
    	balancers.add(Constants.LOAD_BALANCER);
    	List<String> zones = new ArrayList<>();
    	zones.add(instances.get(0).getPlacement().getAvailabilityZone());
    	
    	autoScale.setAutoScalingGroupName(Constants.AUTOSCALE_GROUP);
    	autoScale.setLoadBalancerNames(balancers);
    	autoScale.setMinSize(2);
    	autoScale.setMaxSize(6);
    	autoScale.setDefaultCooldown(120);
    	autoScale.setAvailabilityZones(zones);
    	autoScale.setLaunchConfigurationName("MyLaunchConfig");
    	
    	/*Create Autoscale group*/
    	AmazonAutoScalingClient client = new AmazonAutoScalingClient(new DefaultAWSCredentialsProviderChain().getCredentials());
    	client.withRegion(Regions.US_WEST_2);
    	client.createAutoScalingGroup(autoScale);
    	createScalingPolicyAndAlarm(client);
    }
    
    public static void createAndAttachLoadBalancer(List<Instance> instances) throws AmazonServiceException
    {
    	List<String> availabilityZones = new ArrayList<String>();
    	Instance instance = instances.get(0);
    	Collection<com.amazonaws.services.elasticloadbalancing.model.Instance> instanceIDs = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
    	com.amazonaws.services.elasticloadbalancing.model.Instance ins = new com.amazonaws.services.elasticloadbalancing.model.Instance();
    	ins.setInstanceId(instance.getInstanceId());
    	instanceIDs.add(ins);
    	availabilityZones.add(instance.getPlacement().getAvailabilityZone());
    	
    	/*configure listeners*/
    	List<Listener> listeners = new ArrayList<Listener>();
    	listeners.add(new Listener("HTTP", 80, 80));
    	listeners.add(new Listener("TCP", 443, 443));
    	CreateLoadBalancerRequest request = new CreateLoadBalancerRequest(Constants.LOAD_BALANCER);
    	request.setAvailabilityZones(availabilityZones);
    	request.setListeners(listeners);
    	
    	/*Set policy*/
    	SetLoadBalancerPoliciesOfListenerRequest setLoadBalancerPoliciesOfListenerRequest = new SetLoadBalancerPoliciesOfListenerRequest();
    	setLoadBalancerPoliciesOfListenerRequest.setLoadBalancerPort(80);
    	
    	/*Health check parameters*/
    	HealthCheck healthCheck = new HealthCheck();
    	healthCheck.setHealthyThreshold(6);
    	healthCheck.setUnhealthyThreshold(2);
    	healthCheck.setTimeout(2);
    	healthCheck.setInterval(30);
    	healthCheck.setTarget("HTTP:80/");
    	
    	/*Configure health check*/
    	ConfigureHealthCheckRequest healthCheckReq = new ConfigureHealthCheckRequest();
    	healthCheckReq.setHealthCheck(healthCheck);
    	healthCheckReq.setLoadBalancerName(Constants.LOAD_BALANCER);
    	
    	/*Create Client and create load balancer*/
    	AmazonElasticLoadBalancingClient client = new AmazonElasticLoadBalancingClient(new DefaultAWSCredentialsProviderChain().getCredentials());
    	client.setEndpoint("https://elasticloadbalancing.us-west-2.amazonaws.com");
    	CreateLoadBalancerResult res = client.createLoadBalancer(request);
    	System.out.println("Loadbalancer created with "+res.getDNSName());
    	
    	/*Set health check*/
    	ConfigureHealthCheckResult result = client.configureHealthCheck(healthCheckReq);
    	System.out.println("Health Check configured "+result.getHealthCheck());
    	
    	/*Register load balancer with request*/
    	RegisterInstancesWithLoadBalancerRequest registerReq = new RegisterInstancesWithLoadBalancerRequest();
    	registerReq.withInstances(instanceIDs);
    	registerReq.setLoadBalancerName(Constants.LOAD_BALANCER);
    	RegisterInstancesWithLoadBalancerResult registerWithLoadBalancerResult= client.registerInstancesWithLoadBalancer(registerReq);
    	System.out.println(registerWithLoadBalancerResult.toString());
    }
    
    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to AWS!");
        System.out.println("===========================================");
        System.out.println("Now creating an EC2 Client !");
        ec2 = new AmazonEC2Client();//create a new Client
        try {
        	List<Instance> instances = listActiveInstances();
        	createAndAttachLoadBalancer(instances);
        	CreateLaunchConfigurationRequest config = createAutoScaleConfig();
        	createAutoscaleGroup(instances, config);
        } catch (AmazonServiceException ase) {
           System.out.println("error !"+ase.getErrorMessage());
        }
        
    }
}
