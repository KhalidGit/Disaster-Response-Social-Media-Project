package hackoverflow.disasterreliefnetwork.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by Ali on 10/28/2017.
 */

public class TwitterScraper {
    private Context context;
    private JSONArray jsonArray;
    private List<Tweet> tweets;

    public TwitterScraper(Context context) {
        this.context = context;
        this.jsonArray = new JSONArray();
        this.tweets = new ArrayList<>();
    }

    public void initializeTwitter() {
        Twitter.initialize(context);
        TwitterConfig config = new TwitterConfig.Builder(context)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("CONSUMER_KEY", "CONSUMER_SECRET"))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    public void scrapeHashtag(String searchQuery) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        SearchService searchService = twitterApiClient.getSearchService();
        Call<Search> call = searchService.tweets(searchQuery, null, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Search>() {
            @Override
            public void success(Result<Search> result) {
                tweets = new ArrayList<>();
                for (Tweet tweet : result.data.tweets) {
                    if (tweet.retweetedStatus == null)
                        tweets.add(tweet);
                }
                try {
                    convertTweetsToJSON();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void convertTweetsToJSON() throws JSONException {
        for (Tweet tweet : tweets) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", tweet.id);
            jsonObject.put("place_coords", (tweet.place != null) ? tweet.place.boundingBox.coordinates : null);
            jsonObject.put("geo_coords", (tweet.coordinates != null) ? tweet.coordinates.coordinates : null);
            jsonObject.put("text", tweet.text);
            jsonObject.put("user", tweet.user);
            jsonObject.put("resolved", false);
            this.jsonArray.put(jsonObject);
        }
        //TODO: Make callback
    }
}
