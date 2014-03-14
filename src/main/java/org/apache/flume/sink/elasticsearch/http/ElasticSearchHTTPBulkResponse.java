package org.apache.flume.sink.elasticsearch.http;

import java.util.ArrayList;
import java.util.List;

import org.aicer.hibiscus.http.client.Response;

import com.google.gson.Gson;

public class ElasticSearchHTTPBulkResponse {

  private String responseString = null;

  private BulkResponse responseObject = null;

  private Gson gson = new Gson();

  private String httpResponseStatusLine = null;

  private int httpResponseCode = 200;

  public ElasticSearchHTTPBulkResponse(final Response originalResponse) {
    processRawJson(originalResponse);
  }

  private void processRawJson(final Response originalResponse) {

    this.responseString = originalResponse.getResponseBody();
    this.httpResponseCode = originalResponse.getResponseCode();
    this.httpResponseStatusLine = originalResponse.getStatusLine();

    if (originalResponse.getResponseCode() == 200) {
      responseObject = gson.fromJson(responseString, BulkResponse.class);
    }
  }

  public boolean hasFailures() {


    if (responseObject != null) {

      for (BulkResponseItem item : this.responseObject.getResponseItems()) {

        if (false == item.getCreateResponseItemDetails().isOk()){

          return true;
        }
      }
    }

    return true;
  }

  public String buildFailureMessage() {

    if (responseObject != null) {

      String failureMessage = "";

        for (BulkResponseItem item : this.responseObject.getResponseItems()) {

          if (false == item.getCreateResponseItemDetails().isOk()){

            failureMessage += item.getCreateResponseItemDetails().toString() + "\n";
          }
        }

      return failureMessage;
    }

    return this.httpResponseCode + "\n" +
    this.httpResponseStatusLine + "\n" +
    this.responseString;
  }

  /**
   * Contains details about the create operation
   *
   * @author iekpo
   *
   */
  @SuppressWarnings("unused")
  private class BulkCreateResponseItemDetails {

    private String _index = null;

    private String _type = null;

    private String _id = null;

    private int _version = 1;

    private boolean ok = false;

    private String error = null;

    @Override
    public String toString() {

      return "[" + "_index:" + this._index + ", " +

                   "_type:" + this._type + ", " +

                   "_id:" + this._id + ", " +

                   "_version:" + this._version + ", " +

                   "error:" + this.error + "]\n";
    }

    public int getVersion() {
      return this._version;
    }

    public String getId() {
      return this._id;
    }

    public String getType() {
      return this._type;
    }

    public String getIndex() {
      return this._index;
    }

    public boolean isOk() {
      return this.ok;
    }

    public String getError() {
      return this.error;
    }
  }

  /**
   * Represents a response entry in the HTTP Response from ES
   *
   * @author iekpo
   *
   */
  private class BulkResponseItem {

    private final BulkCreateResponseItemDetails create = new BulkCreateResponseItemDetails();

    public BulkCreateResponseItemDetails getCreateResponseItemDetails() {
      return this.create;
    }
  }

  /**
   * A collection on responses from ES
   *
   * @author iekpo
   *
   */
  @SuppressWarnings("unused")
  private class BulkResponse {

    private long took;

    private final List<BulkResponseItem> items = new ArrayList<BulkResponseItem>();

    public List<BulkResponseItem> getResponseItems() {
      return this.items;
    }

    public long getElapsedTime() {
      return this.took;
    }
  }
}
