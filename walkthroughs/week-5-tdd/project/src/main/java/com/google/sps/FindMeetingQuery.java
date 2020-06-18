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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> meetings = new ArrayList<TimeRange>();
    ArrayList<Event> eventTimes = new ArrayList<Event>(events);
    ArrayList<String> requestAttendees = new ArrayList(request.getAttendees());
    ArrayList<String> requestOptionalAttendees = new ArrayList(request.getOptionalAttendees());
    requestOptionalAttendees.addAll(requestAttendees);
    Collections.sort(eventTimes, Comparator.comparing(Event::getWhen, TimeRange.ORDER_BY_START));
    long duration = request.getDuration();
    if(duration > 23 * 60 + 59){
      return meetings;
    }

    if(!request.getOptionalAttendees().isEmpty()){
      ArrayList<TimeRange> optionalbusyTimes = new ArrayList<TimeRange>();
      for (Event event: eventTimes){
        if(attendentAttending(event.getAttendees(), requestOptionalAttendees)){
          if(optionalbusyTimes.isEmpty()){
            optionalbusyTimes.add(event.getWhen());
          } else if(optionalbusyTimes.get(optionalbusyTimes.size() - 1).overlaps(event.getWhen())) {
            optionalbusyTimes.set(optionalbusyTimes.size() - 1, 
              TimeRange.fromStartEnd(optionalbusyTimes.get(optionalbusyTimes.size() - 1).start(),  
              Math.max(optionalbusyTimes.get(optionalbusyTimes.size() - 1).end(), event.getWhen().end()), false));
          } else {
            optionalbusyTimes.add(event.getWhen());
          }
        }
      }
      Collections.sort(optionalbusyTimes, TimeRange.ORDER_BY_START);

      if(optionalbusyTimes.get(0).start() > duration){
        meetings.add(TimeRange.fromStartDuration(0, optionalbusyTimes.get(0).start()));
      }
      
      for(int i = 0; i < optionalbusyTimes.size() - 1; i++){
        if(optionalbusyTimes.get(i + 1).start() - optionalbusyTimes.get(i).end() >= duration){
          meetings.add(TimeRange.fromStartEnd(optionalbusyTimes.get(i).end(), optionalbusyTimes.get(i + 1).start(), false));
        }
      }
      
      if((((23 * 60) + 59) - optionalbusyTimes.get(optionalbusyTimes.size() - 1).end()) >= duration){
        meetings.add(TimeRange.fromStartEnd(optionalbusyTimes.get(optionalbusyTimes.size() - 1).end(), 24 * 60, false));
      }

      if(!meetings.isEmpty()){
        return meetings;
      }
    }
    
    ArrayList<TimeRange> busyTimes = new ArrayList<TimeRange>();
    for (Event event: eventTimes){
      if(attendentAttending(event.getAttendees(), requestAttendees)){
        if(busyTimes.isEmpty()){
          busyTimes.add(event.getWhen());
        } else if(busyTimes.get(busyTimes.size() - 1).overlaps(event.getWhen())) {
          busyTimes.set(busyTimes.size() - 1, 
            TimeRange.fromStartEnd(busyTimes.get(busyTimes.size() - 1).start(),  
              Math.max(busyTimes.get(busyTimes.size() - 1).end(), event.getWhen().end()), false));
        } else {
          busyTimes.add(event.getWhen());
        }
      }
    }

    meetings = new ArrayList<TimeRange>();
    if(busyTimes.isEmpty()){
      meetings.add(TimeRange.WHOLE_DAY);
      return meetings;
    }

    if(busyTimes.get(0).start() >= duration){
      meetings.add(TimeRange.fromStartDuration(0, busyTimes.get(0).start()));
    }
    for(int i = 0; i < busyTimes.size() - 1; i++){
      if(busyTimes.get(i + 1).start() - busyTimes.get(i).end() >= duration){
        meetings.add(TimeRange.fromStartEnd(busyTimes.get(i).end(), busyTimes.get(i + 1).start(), false));
      }
    }
    if((((23 * 60) + 59) - busyTimes.get(busyTimes.size() - 1).end()) >= duration){
      meetings.add(TimeRange.fromStartEnd(busyTimes.get(busyTimes.size() - 1).end(), 24 * 60, false));
    }

    return meetings;
  }



  /* Returns true if an attendee is in the event attendees **/
  private boolean attendentAttending(Set<String> eventAttendees, ArrayList<String> attendees) {
    for(int i = 0; i < attendees.size(); i++){
      if(eventAttendees.contains(attendees.get(i))){
        return true;
      }
    }
    return false;
  }
}
