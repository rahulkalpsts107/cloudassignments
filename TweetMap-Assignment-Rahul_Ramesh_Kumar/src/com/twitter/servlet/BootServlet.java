package com.twitter.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.twitter.engine.TwitterClient;

/**
 * Servlet implementation class BootServlet
 */
public class BootServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BootServlet() {
        super();
        // TODO Auto-generated constructor stub
        Runnable T = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				new TwitterClient().new TwitterConsumer().run();
			}
		};
		/* commented data Injector since we have enough data for now.
		Thread dispatch = new Thread(T);
		dispatch.start();
        System.out.println("Twitter Consumer is up");
        */
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
