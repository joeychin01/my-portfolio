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

/** get messages from datastore and display them */
function getMessages() {
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

// TODO: fix formatting of comments
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';
  const bodyElement = document.createElement('span');
  bodyElement.innerText = comment.comment + "\n" + "\n";

  const authorElement = document.createElement('span');
  authorElement.innerText = comment.author + "\n";
  
  commentElement.appendChild(authorElement);
  commentElement.appendChild(bodyElement);
  commentElement.style.backgroundColor = comment.color;
  return commentElement;
}


/** deletes all comments */
function deleteEverything(){
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request).then(response => response.json()).then(() => {
    getMessages();
  });
}