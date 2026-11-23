package com.aminsula.pilot.order;

import java.util.ArrayList;
import java.util.List;

/** The agent's customers for today (date is the server's date, yyyy-MM-dd), in visit order. */
public class AgentDayDto {

    private String date;
    private List<AgentVisitDto> visits = new ArrayList<>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<AgentVisitDto> getVisits() {
        return visits;
    }

    public void setVisits(List<AgentVisitDto> visits) {
        this.visits = visits;
    }
}
