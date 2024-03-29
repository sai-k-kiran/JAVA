import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class rest {
    public static int getTotalGoals(String team, int year) throws IOException {
        String URL = "https://jsonmock.hackerrank.com/api/football_matches";

        int goalsByTeam1 = getTeamGoals(
                String.format(URL + "?year=%d&team1=%s", year, URLEncoder.encode(team, "UTF-8")),
                "team1",
                1, 0
        );

        int goalsByTeam2 = getTeamGoals(
                String.format(URL + "?year=%s&team2=%s", year, URLEncoder.encode(team, "UTF-8")),
                "team2",
                1, 0
        );

        return goalsByTeam1 + goalsByTeam2;
    }

    public static int getTeamGoals(String endpoint, String team, int page, int totalGoals) throws IOException{
        String s = getResponsePerPage(endpoint, page);

        JsonObject jsonResponse = new Gson().fromJson(s.toString(), JsonObject.class);
        int totalPages = jsonResponse.get("total_pages").getAsInt();
        JsonArray data = jsonResponse.getAsJsonArray("data");
        for (JsonElement e : data) {
            totalGoals += e.getAsJsonObject().get(team +"goals").getAsInt();
        }

        return (page < totalPages) ? getTeamGoals(endpoint, team, page+1, totalGoals) : totalGoals;
    }


    public static int winnerGoals(String name, int year) throws IOException{
        final String CURL = "https://jsonmock.hackerrank.com/api/football_competitions";

        String URL = String.format(CURL + "?name=%s&year=%d", URLEncoder.encode(name, "UTF-8"), year);

        String winner = winningTeam(URL);
        System.out.println("Winner of " + name + " in " + year +": " + winner);
        return getTotalGoals(winner, year);
    }

    public static String winningTeam(String URL) throws IOException{
        String res = getResponsePerPage(URL, 1);

        JsonObject response = new Gson().fromJson(res, JsonObject.class);
        JsonElement element = response.getAsJsonArray("data").get(0);

        return element.getAsJsonObject().get("winner").getAsString();
    }

    public static String getResponsePerPage(String URL, int page) throws IOException{
        URL url = new URL(URL + "&page=" + page);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.addRequestProperty("Content-type", "application/json");

        int status = conn.getResponseCode();

        if(status < 200 || status >= 300)
            throw new IOException("Cannot connect");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String res;
        StringBuilder s = new StringBuilder();

        while((res = br.readLine()) != null)
            s.append(res);

        br.close();
        conn.disconnect();

//        YOU CAN USE SCRIPTENGINEMANAGER TO WRITE AND ACCESS JAVASCRIPT
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("javascript"); // you need org.mozilla.javascrpit jar
//
//        String script = "var obj = JSON.parse('"+ s.toString() +"')";
//        script += "var total_pages = obj.total_pages";
//        script += "var total_goals = obj.data.reduce(function(acc, curr)" +
//                "{return acc + parseInt(curr."+ team +"goals)}, 0)";

//        return (int) engine.get("total_goals");

        return s.toString();
    }

    public static void main(String[] args) throws IOException {
        String team = "Manchester City";
        int year = 2011;
        int res = rest.getTotalGoals(team, year);
        System.out.println("Goals by " + team + " in year "+year+ ": " +res);

        int res2 = winnerGoals("English Premier League", 2011);
        System.out.println("Winning team scored: " + res2 + " goals");
    }
}
