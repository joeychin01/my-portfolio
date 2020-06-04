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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
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
    String comment = request.getParameter("comment-box");
    long timestamp = System.currentTimeMillis();
    String author = request.getParameter("author-box");
    String color = request.getParameter("favcolor");
    if(author.length() == 0) {
      author = "Anonymous";
    }
    if(comment.length() > 0){
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("comment", comment);
      commentEntity.setProperty("timestamp", timestamp);
      commentEntity.setProperty("author", author);
      commentEntity.setProperty("color", color);
      datastore.put(commentEntity);
    }
    response.sendRedirect("/index.html");
  }

  /** Function Returns list of comments */
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
    PreparedQuery results = datastore.prepare(query);
    int numComments = Integer.parseInt(request.getParameter("num"));
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if(comments.size() <= numComments) {
        long id = entity.getKey().getId();
        String text = (String) entity.getProperty("comment");
        long timestamp = (long) entity.getProperty("timestamp");
        String author = (String) entity.getProperty("author");
        String color = (String) entity.getProperty("color");
        Comment comment = new Comment(id, text, timestamp, author, color);
        comments.add(comment);
      }
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(comments));
  }
}