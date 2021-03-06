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

package com.google.sps.data;
/** Holds the info of a comment */
public final class Comment {

  private final long id;
  private final String author;
  private final String comment;
  private final String color;
  private final long timestamp;
  private final String textColor;
  private final double sentimentScore;
  private final boolean delete;
  private final String email;

  /** Represents a comment
    * id - unique id for the comment
    * comment - the body of the comment
    * timestamp - the time the comment was sent
    * author - nickname of the user
    * color - background color
    * textcolor - text color
    * sentimentScore - sentiment analysis score (-1 to 1)
    * delete - if the comment should have the delete button added to it
    * email - the email of the user that sent the message
   */
  public Comment(long id, String comment, long timestamp, String author, String color, String textColor, double sentimentScore, boolean delete, String email) {
    this.id = id;
    this.comment = comment;
    this.timestamp = timestamp;
    this.author = author;
    this.color = color;
    this.textColor = textColor;
    this.sentimentScore = sentimentScore;
    this.delete = delete;
    this.email = email;
  }
}