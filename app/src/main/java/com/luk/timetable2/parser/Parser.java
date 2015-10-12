package com.luk.timetable2.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luk on 5/16/15.
 */
public class Parser {
    private String mUrl;
    private Pattern mRegexGroup = Pattern.compile("-[a-zA-z0-9/]+$");

    private String QUERY_CLASSES_SELECT = "select[name=oddzialy]";
    private String QUERY_CLASSES_A = "a[target=plan]";

    private String QUERY_TABLE = "table[class=tabela]";
    private String QUERY_LESSON = "td[class=l]";
    private String QUERY_LESSON_MULTIPLE = "span[style=font-size:85%]";
    private String QUERY_SUBJECT = "span[class=p]";
    private String QUERY_HOUR = "td[class=g]";
    private String QUERY_ROOM = ".s";

    /**
     * @param url Vulcan API link
     */
    public Parser(String url) {
        this.mUrl = url;
    }

    /**
     * @return list of classes
     * @throws IOException
     */
    public HashMap<Integer, String> parseClasses() throws IOException {
        HashMap<Integer, String> classes = new HashMap<>();

        Document data = Jsoup.connect(mUrl).header("Accept", "*/*").get();

        Elements classes_select = data.select(QUERY_CLASSES_SELECT);
        Elements classes_a = data.select(QUERY_CLASSES_A);

        if (classes_select.size() > 0) {
            for (Element c : classes_select.select("option")) {
                if (c.hasAttr("value")) {
                    classes.put(Integer.parseInt(c.attr("value")), c.html());
                }
            }

            return classes;
        }

        for (Element c : classes_a) {
            if (c.attr("href").startsWith("plany/o")) {
                classes.put(
                        Integer.parseInt(c.attr("href").substring(7, c.attr("href").length() - 5)),
                        c.html()
                );
            }
        }

        return classes;
    }

    /**
     * Should return list of lessons for specific class.
     *
     * @return list of lessons
     * @throws IOException
     */
    public HashMap<Integer, ArrayList<Lesson>> parseLessons() throws IOException {
        ArrayList<String> hours = new ArrayList<>();
        HashMap<Integer, ArrayList<Lesson>> lessons = new HashMap<>();
        Document data = Jsoup.connect(mUrl).header("Accept", "*/*").get();
        Elements table = data.select(QUERY_TABLE);
        Elements tr = table.select("tr");

        for (Element hour : table.select(QUERY_HOUR)) {
            String h = hour.html();
            h = h.replace("- ", "-");

            hours.add(h);
        }

        for (int i = 1; i < tr.size(); i++) {
            int day = 1;
            String hour = tr.get(i).select(QUERY_HOUR).html().replace("- ", "-");
            hours.add(hour);

            Elements elementsLessons = tr.get(i).select(QUERY_LESSON);

            for (Element lesson : elementsLessons) {
                ArrayList<Lesson> array = (lessons.get(day) == null) ?
                        new ArrayList<Lesson>() : lessons.get(day);

                if (lesson.select(QUERY_LESSON_MULTIPLE).size() > 0 &&
                        lesson.select(QUERY_SUBJECT).size() <
                                lesson.select(QUERY_LESSON_MULTIPLE).size()) {
                    for (int g = 0; g < lesson.select(QUERY_LESSON_MULTIPLE).size(); g++) {
                        array.add(parseLesson(lesson, hours.get(i - 1), g));

                        lessons.put(day, array);
                    }
                } else {
                    try {
                        if (lesson.select(QUERY_SUBJECT).size() > 1 &&
                                lesson.select(QUERY_ROOM).size() > 0) {
                            String[] _groups = lesson.html().split("<br>");

                            for (String group : _groups) {
                                Element elem = Jsoup.parse(group);
                                array.add(parseLesson(elem, hours.get(i - 1), 0));

                                lessons.put(day, array);
                            }
                        } else {
                            array.add(parseLesson(lesson, hours.get(i - 1), 0));

                            lessons.put(day, array);
                        }
                    } catch (Exception ex) {
                        // Do nothing, no lesson that time
                    }
                }

                day++;
            }
        }

        return lessons;
    }

    /**
     * Should return lesson in this scheme:
     * lesson: [hour, lesson_name, group, room]
     * Used internally.
     *
     * @return list of lessons
     */
    private Lesson parseLesson(Element lesson, String hour, Integer num) {
        String name = lesson.select(QUERY_SUBJECT).get(num).html();
        String room = "";
        String group = "";

        // make _lesson uppercase
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        // get room
        try {
            room = lesson.select(QUERY_ROOM).get(num).html();
        } catch (Exception ex) {
            // do nothing, no room
        }

        // get group
        Matcher match = mRegexGroup.matcher(name);

        if (match.find()) {
            name = name.replace(match.group(0), ""); // remove group from lesson
            group = match.group(0).substring(1);
        } else {
            for (String line : lesson.html().split("\n")) {
                match = mRegexGroup.matcher(line.trim());

                if (match.find()) {
                    group = match.group(num).substring(1);
                    break;
                }
            }
        }

        return new Lesson(name, room, hour, group);
    }

}