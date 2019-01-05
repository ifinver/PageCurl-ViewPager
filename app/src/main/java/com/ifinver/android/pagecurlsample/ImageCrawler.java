//package com.ifinver.android.pagecurlsample;
//
//import android.os.Handler;
//import android.os.Looper;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by iFinVer on 2019/1/4.
// * ilzq@foxmail.com
// */
//public class ImageCrawler {
//
//    private static final String INIT_URL = "https://www.mzitu.com/86712/";
//
//    public static void fetch(final int max, final Listener listener) {
//        new Thread() {
//            @Override
//            public void run() {
//                List<ImageVO> voList = new ArrayList<>(max);
//                List<String> atlasList = new ArrayList<>();
//
//                //获取网站资源
//                Document document = null;
//                try {
//                    atlasList.add(INIT_URL);
//                    document = Jsoup.connect(INIT_URL).get();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    listener.onFailed(e);
//                }
//
//                if (document == null) {
//                    return;
//                }
//
//                int suffix = 1;
//                for (String atlasUrl : atlasList) {
//                    //获取网站资源
//                    while (true) {
//                        Document atlasDoc = null;
//                        try {
//                            atlasDoc = Jsoup.connect(atlasUrl + suffix).get();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            if (voList.size() > 0) {
//                                postSuccess(voList, listener);
//                            } else {
//                                postFailed(listener,e);
//                            }
//                        }
//
//                        if (atlasDoc == null) {
//                            return;
//                        }
//
//                        //获取网站资源图片
//                        Elements elements = document.select("img[src]");
//                        //循环读取
//
//                        if (elements != null) {
//                            for (int i = 0, elementsSize = elements.size(); i < elementsSize && i < max; i++) {
//                                Element e = elements.get(i);
//                                if (e != null) {
//                                    ImageVO vo = new ImageVO();
//                                    vo.name = e.attr("alt");
//                                    vo.url = e.attr("src");
//                                    if(vo.url == null){
//                                        continue;
//                                    }
//                                    voList.add(vo);
//                                }
//                            }
//                        }
//
//                        if (voList.size() < max) {
//                            final String currUrl = document.baseUri();
//                            if (currUrl != null && !currUrl.contains(atlasUrl + suffix)) {
//                                //图集结束,寻找新的图集
//                                suffix = 1;
//                                break;
//                            } else {
//                                suffix++;
//                            }
//                        }else{
//                            break;
//                        }
//                    }
//                }
//
//                if (voList.size() > 0) {
//                    postSuccess(voList, listener);
//                } else {
//                    postFailed(listener,new IOException("no pictures find"));
//                }
//            }
//        }.start();
//    }
//
//    private static void postFailed(final Listener listener, final Exception e) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                listener.onFailed(e);
//            }
//        });
//    }
//
//    private static void postSuccess(final List<ImageVO> voList, final Listener listener) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                listener.onSuccess(voList);
//            }
//        });
//
//    }
//
//    public interface Listener {
//
//        void onSuccess(List<ImageVO> voList);
//
//        void onFailed(Exception e);
//    }
//}
