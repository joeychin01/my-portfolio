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

/**
 * Adds a random book recommendation to the page.
 */
function addRandomRecommendation() {
  const recommendations =
      ['The Stand', '11/22/63', '\'Salem\'s Lot', 'Carrie', 'Cell', 'Cujo', 'The Dark Tower', 'The Shining', 'Revival', 'Needful Things', 'Mr. Mercedes', 'Misery'];

  // Pick a random recommendation.
  const recommendation = recommendations[Math.floor(Math.random() * recommendations.length)];

  // Add it to the page.
  const recommendationContainer = document.getElementById('recommendation-container');
  recommendationContainer.innerText = recommendation;
}

/** Get messages from datastore and display them */
function getMessages() {
  updateLogin();
  var numComments = document.getElementById("num-comments").value;
  var sortSelection = document.getElementById("sort-selection").value;
  fetch('/messages?num='+numComments+'&sort='+sortSelection).then(response => response.json()).then((comments) => {
    const commentListElement = document.getElementById('comment-list');
    document.getElementById("comment-list").innerHTML = "";
    comments.forEach((comment) => {
      commentListElement.appendChild(createCommentElement(comment));
    })
  });
}

/** Creates one comment element and adds it to the dom */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';
  const bodyElement = document.createElement('span');
  bodyElement.innerText = comment.comment + "\n" + "\n";

  const authorElement = document.createElement('span');
  authorElement.innerText = comment.author + " " + "\n";

  const emotionElement = document.createElement('span');
  let emotion = "";
  if(comment.sentimentScore > 0.3){
    emotion = "happy!";
  } else if(comment.sentimentScore < -0.3) {
    emotion = "down"
  } else {
    emotion = "neutral";
  }
  emotionElement.innerText = "Feeling " + emotion + "\n\n";

  var d = new Date(comment.timestamp);
  const timeElement = document.createElement('span');
  timeElement.innerText = d.toDateString() + "\n";
  timeElement.style.textAlign = "left";

  const deleteButtonElement = document.createElement('button');
  if(comment.delete){
    deleteButtonElement.innerText = 'Delete';
    deleteButtonElement.addEventListener('click', () => {
      deleteComment(comment);

      // Remove the task from the DOM.
      commentElement.remove();
    });
  }


  commentElement.appendChild(authorElement);
  commentElement.appendChild(emotionElement);
  commentElement.appendChild(bodyElement);
  commentElement.appendChild(timeElement);
  if(comment.delete){
    commentElement.appendChild(deleteButtonElement);
  }
  commentElement.style.color = comment.textColor
  commentElement.style.backgroundColor = comment.color;
  return commentElement;
}


/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}



/** Deletes all comments */
function deleteEverything(){
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request).then(response => response.json()).then(() => {
    getMessages();
  });
}

/** Updates page to show login/not login data */
function updateLogin(){
  fetch('/login').then(response => response.json()).then((input) => {
    loginWelcome = document.getElementById("login-welcome");
    loginLink = document.getElementById("login-link");
    if(input.login == "true"){
      console.log(input.userEmail + " " + input.logoutUrl);
      loginWelcome.innerText = "Welcome\n" + input.userEmail;
      loginLink.href = input.logoutUrl;
      loginLink.innerText = "Logout here";
      document.getElementById("comment-form").style.display = "block";
      document.getElementById("comment-display").innerText = "Leave a comment:";
    }
    else{
      loginWelcome.innerText = "Welcome!";
      loginLink.href = input.loginUrl;
      loginLink.innerText = "Login here";
      document.getElementById("comment-form").style.display = "none";
      document.getElementById("comment-display").innerText = "Please sign in to leave a comment";
    }
  });
}

/** Checks if the user has set a nickname and redirects home if they have */
function checkNickname() {
  fetch('/login').then(response => response.json()).then((input) => {
    if(input.hasNick == "true"){
      window.location.href = "/index.html";
    }
  });
}