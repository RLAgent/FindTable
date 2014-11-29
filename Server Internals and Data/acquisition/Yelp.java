/*
 Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
 For a more complete example (how to integrate with GSON, etc) see the blog post above.
 */

import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Example for accessing the Yelp API.
 */
public class Yelp {

  OAuthService service;
  Token accessToken;

  /**
   * Setup the Yelp API OAuth credentials.
   *
   * OAuth credentials are available from the developer site, under Manage API access (version 2 API).
   *
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
  }

  /**
   * Search with term and location.
   *
   * @param term Search term
   * @param latitude Latitude
   * @param longitude Longitude
   * @return JSON string response
   */
  public String search(String term, String latitude, String longitude, int offset) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("limit", "20");
    //request.addQuerystringParameter("sort", "0");
    request.addQuerystringParameter("offset",Integer.toString(offset));
    request.addQuerystringParameter("ll", latitude + "," + longitude);
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

  // CLI
  public static void main(String[] args) throws IOException {
    // Update tokens here from Yelp developers site, Manage API access.
    String consumerKey = "_Zzp-P9R6Lr_w_GFPAwXCg";
    String consumerSecret = "B-UlyC33_kZMuNLg4AWyvHNu6HE";
    String token = "twuX2RYjyuzQqPHSzcwqBrWN_-p4135X";
    String tokenSecret = "MsljTRN5pj6XUj59j2nkErA-Mh0";
    
    BufferedReader f = new BufferedReader(new FileReader("in.txt"));
    // input file name goes above
    
    // Use StringTokenizer vs. readLine/split -- lots faster
    StringTokenizer st = new StringTokenizer("  K   K 1 K  1 ");
    int resultCounter = 0;
    
    Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
    //for (int i = 0; i < 70; i++)
    //    f.readLine();
    int counter = 0;
    String response;
    PrintWriter out;
    /*for (int j = 20; j < 462; j += 20)
    {
        counter ++;
    response = yelp.search("restaurant", "39.3799986","-74.453348", j);
    // Edison: "40.5381495","-74.3780251"
    // Trenton: "40.2160535","-74.7742055"
    // Atlantic City: "39.3799986","-74.453348"
    //
    out = new PrintWriter(new BufferedWriter(new FileWriter("newresults2/result" + (3377 + counter) + ".json")));
    out.println(response);
    out.close();
    }
    System.exit(0); */
    for (int i = 0; i < 100; i++)
    {
        //resultCounter += 100; //debug
        st = new StringTokenizer(f.readLine(),",");
        String latitude = st.nextToken();
        String longitude = st.nextToken();
        int total = 50*20;
        for (int j = 0; j < 50; j++)
        {
            
            resultCounter++;
            response = yelp.search("restaurant", latitude, longitude, j * 20);
            String[] v = response.split("\"total\": ");
            if (v.length > 1)
            {
                v = v[1].split(",");
                //st = new StringTokenizer(st.nextToken(),",");
                total = Integer.parseInt(v[0]);
                System.out.println(total);
            }
            if (total > 1000) total = 1000;
            out = new PrintWriter(new BufferedWriter(new FileWriter("newresults2/result" + resultCounter + ".json")));
            out.println(response);
            out.close();
            System.out.println(i + "." + (j*200/total) + "%");
            if ((j+1) * 20 > total)
                break;
        }
    }
    

    //System.out.println(response);
  }
}
