package com.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.PersonData;
import org.json.JSONObject;

public class AngularJsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public AngularJsServlet() {
        super();
    }

    public static Integer brainState = 1;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        PersonData personData = new PersonData();
        personData.setFirstName("Katya");
        personData.setLastName("Donetski");
        JSONObject respObject = new JSONObject(personData);
        response.setContentType("application/json");
        response.getWriter().write(respObject.toString());
    }
}