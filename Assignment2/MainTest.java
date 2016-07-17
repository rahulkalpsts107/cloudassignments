package com.aws.assignment1;
import java.util.ArrayList;
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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.aws.assignment1.util.Constants; 

public class MainTest {
	public static AmazonEC2 ec2 = null;
	
	public static RunInstancesResult createInstance(){		
		RunInstancesRequest runInstancesRequest =
			      new RunInstancesRequest();
		
		runInstancesRequest.withImageId("ami-f0091d91")
		 .withInstanceType("t2.micro")
		 .withMinCount(1)
		 .withMaxCount(1)
		 .withKeyName("rahulramesh-keypair-oregon");
		
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
		System.out.println(runInstancesResult.getReservation().getReservationId());
		return runInstancesResult;
	}

    public static List<Instance> listActiveInstances() throws AmazonClientException {
        ec2 = new AmazonEC2Client();
        ec2.setRegion(Region.getRegion(Regions.US_WEST_2));
    	List<Instance> instances = new ArrayList<Instance>();
    	List<String> serviceInstanceIds = new ArrayList<String>();
    	serviceInstanceIds.add(Constants.INSTANCE1);
    	serviceInstanceIds.add(Constants.INSTANCE2);
    	DescribeInstancesRequest request = new DescribeInstancesRequest();
    	request.setInstanceIds(serviceInstanceIds);
           DescribeInstancesResult response = ec2.describeInstances(request);

    	List<Reservation> reservations = response.getReservations();
    	System.out.println("Reservations found "+reservations.size());
    	for (Reservation reservation : reservations) {
    		System.out.println("Reservation Id "+reservation.getReservationId());
    		for (Instance instance : reservation.getInstances()) {
    			System.out.println(instance.getInstanceId());
    			System.out.println(instance.getImageId());
    			System.out.println(instance.getPublicIpAddress());
    			System.out.println(instance.getState().getName());
    			System.out.println("***********************");
    		}
    	}

    	return instances;
    }
    
    public static void deleteInstance(RunInstancesResult runInstancesResult){
    	TerminateInstancesRequest terminate = new TerminateInstancesRequest();
		List<String> l = new ArrayList<String>();
		l.add(runInstancesResult.getReservation().getInstances().get(0).getInstanceId());
		terminate.setInstanceIds(l);
		TerminateInstancesResult result = ec2.terminateInstances(terminate);
		System.out.println(result.toString());
    }

    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to AWS!");
        System.out.println("===========================================");
        try {
        	listActiveInstances();
        	System.out.println("Now creating an EC2 Instance !");
        	RunInstancesResult result = createInstance();
        	//Thread.sleep(30000);
        	//System.out.println("Going to terminate instance !");
        	//deleteInstance(result);
        	
        } catch (AmazonServiceException ase) {
           System.out.println("error !"+ase.getErrorMessage());
        }
        
    }
}
