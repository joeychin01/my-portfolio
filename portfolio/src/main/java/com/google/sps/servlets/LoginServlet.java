// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/** Servlet returns login URLs */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  
  /** Function returns login URLs */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    UserService userService = UserServiceFactory.getUserService();
    HashMap<String, String> input = new HashMap<String, String>();
    if (userService.isUserLoggedIn()) {
      input.put("login", "true");
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      input.put("logoutUrl", logoutUrl);
      input.put("userEmail", userEmail);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      String id = userService.getCurrentUser().getUserId();
      Query query = new Query("Nickname")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        input.put("hasNick", "false");
      } else {
        input.put("hasNick", "true");
      }
      
    } else {
      input.put("login", "false");
      String urlToRedirectToAfterUserLogsIn = "/nickname.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      input.put("loginUrl", loginUrl);
    }
    response.getWriter().println(gson.toJson(input));
  }
}