/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.pubsub;

import com.google.api.core.ApiFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;

@WebServlet(name = "PubSub", value = "/")
public class PubSub extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String topicId = System.getenv("PUBSUB_TOPIC");

      ProjectTopicName topicName =
          ProjectTopicName.newBuilder()
              .setProject(ServiceOptions.getDefaultProjectId())
              .setTopic(topicId)
              .build();
      Publisher publisher = Publisher.newBuilder(topicName).build();

      final String payload = req.getParameter("payload");
      PubsubMessage pubsubMessage =
          PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(payload)).build();

      publisher.publish(pubsubMessage);
      // redirect to home page
      ApiFuture<String> future = publisher.publish(pubsubMessage);
      future.get();
      resp.sendRedirect("/");
    } catch (Exception e) {
      resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      if (getServletContext().getAttribute("data") != null) {
        Data data = (Data) getServletContext().getAttribute("data");
        req.setAttribute("messages", data.getMessages());
        req.setAttribute("tokens", data.getTokens());
        req.setAttribute("claims", data.getClaims());
      }
      RequestDispatcher requestDispatcher = req.getRequestDispatcher("index.jsp");
      requestDispatcher.forward(req, resp);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
