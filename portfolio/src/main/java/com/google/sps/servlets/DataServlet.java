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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet controls the comments and datastore */
@WebServlet("/messages")
public class DataServlet extends HttpServlet {

  /** Function adds a comment to datastore */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    String comment = request.getParameter("comment-box");
    long timestamp = System.currentTimeMillis();
    String color = request.getParameter("favcolor");
    String textColor = request.getParameter("text-color");
    String email = userService.getCurrentUser().getEmail();

    if(comment.length() > 0 && userService.isUserLoggedIn()){
      Document doc =
          Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      double score = (double) sentiment.getScore();
      languageService.close();

      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("comment", comment);
      commentEntity.setProperty("timestamp", timestamp);
      commentEntity.setProperty("author", getNickname(userService.getCurrentUser().getUserId()));
      commentEntity.setProperty("color", color);
      commentEntity.setProperty("textColor", textColor);
      commentEntity.setProperty("sentimentScore", score);
      commentEntity.setProperty("email", email);
      datastore.put(commentEntity);
    }
    response.sendRedirect("/index.html");
  }

  /** Function returns list of comments */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");
    String sort = request.getParameter("sort");
    if(sort.equals("time-decreasing")){
      query.addSort("timestamp", SortDirection.DESCENDING);
    } else if(sort.equals("time-increasing")) {
      query.addSort("timestamp", SortDirection.ASCENDING);
    } else if(sort.equals("author-az")) {
      query.addSort("author", SortDirection.ASCENDING);
    } else if(sort.equals("author-za")) {
      query.addSort("author", SortDirection.DESCENDING);
    } else {
      query.addSort("timestamp", SortDirection.DESCENDING);
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    int numComments = Integer.parseInt(request.getParameter("num"));
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");
      String author = (String) entity.getProperty("author");
      String color = (String) entity.getProperty("color");
      String textColor = (String) entity.getProperty("textColor");
      double sentimentScore = (double) entity.getProperty("sentimentScore");
      String email = (String) entity.getProperty("email");
      boolean delete = email.equals(userService.getCurrentUser().getEmail()) || userService.isUserAdmin();
      Comment comment = new Comment(id, text, timestamp, author, color, textColor, sentimentScore, delete, email);
      comments.add(comment);
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(comments));
  }

  /** Function returns nickname from input user id -- maybe put this as a function in comment servlet? */
  private String getNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Nickname")
      .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}