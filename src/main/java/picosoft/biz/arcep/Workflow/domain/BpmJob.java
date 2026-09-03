package picosoft.biz.arcep.Workflow.domain;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BpmJob implements Serializable {

    private Long id;
    private String processID;
    private String activityName;
    private String assignee;
    private ZonedDateTime activityDueDate;
    private Long objectID;
    private Long classID;
    private Boolean endProcess;

    private String data;
    private Object dataObject;
    private List<String> authors;
    private List<String> readers;
    private String processName;

    public BpmJob() {
    }

    public BpmJob(org.json.simple.JSONObject object, String activityName, String assignee, Boolean endProcess, String processInstanceId, Long objectID, Long classID) {

        this.setEndProcess(endProcess);
        this.setActivityName(activityName);
        this.setAssignee(assignee);
        this.setClassID(classID);
        this.setData(object.toJSONString());
        this.setObjectID(objectID);
        this.setProcessID(processInstanceId);
    }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Long getId() {
          return id;
      }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public ZonedDateTime getActivityDueDate() {
        return activityDueDate;
    }

    public void setActivityDueDate(ZonedDateTime activityDueDate) {
        this.activityDueDate = activityDueDate;
    }

    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public Long getClassID() {
        return classID;
    }

    public void setClassID(Long classID) {
        this.classID = classID;
    }

    public Boolean getEndProcess() {
        return endProcess;
    }

    public void setEndProcess(Boolean endProcess) {
        this.endProcess = endProcess;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getReaders() {
        return readers;
    }

    public void setReaders(List<String> readers) {
        this.readers = readers;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("id", this.id != null? this.id : JSONObject.NULL);
        object.put("processID", this.processID != null? this.processID : JSONObject.NULL);
        object.put("activityName", this.activityName != null? this.activityName : JSONObject.NULL);
        object.put("assignee", this.assignee != null? this.assignee : JSONObject.NULL);
        object.put("activityDueDate", this.activityDueDate != null? this.activityDueDate : JSONObject.NULL);
        object.put("objectID", this.objectID != null? this.objectID : JSONObject.NULL);
        object.put("classID", this.classID != null? this.classID : JSONObject.NULL);
        object.put("endProcess", this.endProcess != null? this.endProcess : JSONObject.NULL);
        object.put("data", this.data != null? new JSONObject(this.data) : JSONObject.NULL);
        return object;
    }

    public org.json.simple.JSONObject toJSONSimple() throws ParseException {
        org.json.simple.JSONObject object = new org.json.simple.JSONObject();
        object.put("id", this.id != null? this.id : null);
        object.put("processID", this.processID != null? this.processID : null);
        object.put("activityName", this.activityName != null? this.activityName : null);
        object.put("assignee", this.assignee != null? this.assignee : null);
        object.put("activityDueDate", this.activityDueDate != null? this.activityDueDate : null);
        object.put("objectID", this.objectID != null? this.objectID : null);
        object.put("classID", this.classID != null? this.classID : null);
        object.put("endProcess", this.endProcess != null? this.endProcess : null);
        object.put("data", this.data != null? ((org.json.simple.JSONObject) new JSONParser().parse(this.data)) : null);
        return object;
    }

    public Map<String, Object> toVariables() throws ParseException {
        Map<String, Object> object = new HashMap<>();
        object.put("id", this.id != null? this.id : null);
        object.put("processID", this.processID != null? this.processID : null);
        object.put("activityName", this.activityName != null? this.activityName : null);
        object.put("assignee", this.assignee != null? this.assignee : null);
        object.put("activityDueDate", this.activityDueDate != null? this.activityDueDate : null);
        object.put("objectID", this.objectID != null? this.objectID : null);
        object.put("classID", this.classID != null? this.classID : null);
        object.put("endProcess", this.endProcess != null? this.endProcess : null);
        object.put("data", this.data != null? ((org.json.simple.JSONObject) new JSONParser().parse(this.data)) : null);
        return object;
    }
}

