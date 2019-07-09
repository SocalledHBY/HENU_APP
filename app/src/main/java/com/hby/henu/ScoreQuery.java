package com.hby.henu;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hby.henu.adapter.ScoreQueryItemAdapter;
import com.hby.henu.model.ScoreQueryItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ScoreQuery extends AppCompatActivity {

    private static final String TAG = "ScoreQuery";
    private static final int FAIL = 0;
    private static final int SUCCESS = 1;
    private static String cookie;
    private static LinearLayout scoreQueryLL;
    private ListView scoreQueryLV;
    private List<ScoreQueryItem> scoreQueryItems = new ArrayList<>();
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    Handler scoreHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Elements td;
                    int counter = ((Elements) msg.obj).size();
                    double total = 0;
                    for (Element course : (Elements) msg.obj) {
                        td = course.select("td");
                        scoreQueryItems.add(new ScoreQueryItem(td.get(1).text(), td.get(7).text()));
                        if (!Character.isDigit(td.get(7).text().charAt(0))) {
                            counter--;
                        } else {
                            total += Double.valueOf(td.get(7).text());
                        }
                    }
                    scoreQueryItems.add(new ScoreQueryItem("平均分", String.valueOf(new DecimalFormat("0.0").format(total / counter))));
                    scoreQueryItems.add(new ScoreQueryItem("总分", String.valueOf(total)));
                    scoreQueryLV.setAdapter(new ScoreQueryItemAdapter(ScoreQuery.this, R.layout.score_query_item, scoreQueryItems));
                    break;
                case FAIL:
                    scoreQueryLL.removeView(scoreQueryLV);
                    TextView error = new TextView(ScoreQuery.this);
                    error.setGravity(Gravity.CENTER);
                    error.setPadding(0, 50, 0, 0);
                    error.setTextSize(24);
                    error.setText("没有检索到记录!");
                    scoreQueryLL.addView(error);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_query);

        scoreQueryLL = (LinearLayout) findViewById(R.id.scoreQueryLL);
        scoreQueryLV = (ListView) findViewById(R.id.scoreQueryLV);

        SharedPreferences cookies = getSharedPreferences("cookies", Context.MODE_PRIVATE);
        cookie = cookies.getString("henu", null);

        String xn = getIntent().getStringExtra("year");
        String xn1 = String.valueOf(Integer.valueOf(xn) + 1);
        String term = getIntent().getStringExtra("term");

        RequestBody formBody = new FormBody.Builder()
                .add("sjxz", "sjxz3")
                .add("ysyx", "yscj")
                .add("zx", "1")
                .add("fx", "1")
                .add("xn", xn)
                .add("xn1", xn1)
                .add("xq", term)
                .add("ysyxS", "on")
                .add("sjxzS", "on")
                .add("zxC", "on")
                .add("fxC", "on")
                .add("menucode_current", "")
                .build();
        Request request = new Request.Builder()
                .url("http://xk.henu.edu.cn/student/xscj.stuckcj_data.jsp")
                .header("Host", "xk.henu.edu.cn")
                .header("Proxy-Connection", "keep-alive")
                .header("Content-Length", "102")
                .header("Cache-Control", "max-age=0")
                .header("Origin", "http://xk.henu.edu.cn")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Referer", "http://xk.henu.edu.cn/student/xscj.stuckcj.jsp?menucode=JW130706")
//                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Cookie", cookie)
                .post(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msgScore = scoreHandler.obtainMessage();
                String score = response.body().string();
                Document html = Jsoup.parse(score);
                Elements tbodys = html.select("tbody");
                if ((tbodys.size() != 0)) {
                    Element tbody = tbodys.get(1);
                    Elements trs = tbody.select("tr");
                    msgScore.obj = trs;
                    msgScore.what = SUCCESS;
                } else {
                    msgScore.what = FAIL;
                }
                scoreHandler.sendMessage(msgScore);
            }
        });
    }
}
