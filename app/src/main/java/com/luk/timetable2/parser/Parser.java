package com.luk.timetable2.parser;

import org.apache.commons.lang3.StringUtils;
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
    private String QUERY_TEACHER = ".n";
    private String QUERY_ROOM = ".s";

    private String PATH_CLASSES = "/lista.html";
    private String PATH_LESSONS = "/plany/o%d.html";

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
    public Classes parseClasses() throws IOException {
        String url = mUrl + PATH_CLASSES;
        Document data = Jsoup.connect(url).header("Accept", "*/*").get();

        Elements classes_select = data.select(QUERY_CLASSES_SELECT);
        Elements classes_a = data.select(QUERY_CLASSES_A);

        Classes classes = new Classes();

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
    public LessonGroup parseLessons(int id) throws IOException {
        String url = mUrl + String.format(PATH_LESSONS, id);
        Document data = Jsoup.connect(url).header("Accept", "*/*").get();
        Elements table = data.select(QUERY_TABLE);
        Elements tr = table.select("tr");

        LessonGroup lessonGroup = new LessonGroup();
        ArrayList<String> hours = new ArrayList<>();

        for (Element hour : table.select(QUERY_HOUR)) {
            String h = hour.html();
            h = h.replace("- ", "-");

            hours.add(h);
        }

        for (int i = 1; i < tr.size(); i++) {
            String hour = tr.get(i).select(QUERY_HOUR).html().replace("- ", "-");
            hours.add(hour);

            Elements elementsLessons = tr.get(i).select(QUERY_LESSON);

            for (Element lesson : elementsLessons) {
                int day = elementsLessons.indexOf(lesson) + 1;
                ArrayList<Lesson> lessons = lessonGroup.getLessons(day);

                if (lesson.select(QUERY_LESSON_MULTIPLE).size() > 0 &&
                        lesson.select(QUERY_SUBJECT).size() <
                                lesson.select(QUERY_LESSON_MULTIPLE).size()) {
                    for (int g = 0; g < lesson.select(QUERY_LESSON_MULTIPLE).size(); g++) {
                        lessons.add(parseLesson(lesson, hours.get(i - 1), g));

                        lessonGroup.put(day, lessons);
                    }
                } else {
                    try {
                        if (lesson.select(QUERY_SUBJECT).size() > 1 &&
                                lesson.select(QUERY_ROOM).size() > 0) {
                            String[] groups = lesson.html().split("<br>");

                            for (String group : groups) {
                                Element elem = Jsoup.parse(group);
                                lessons.add(parseLesson(elem, hours.get(i - 1), 0));

                                lessonGroup.put(day, lessons);
                            }
                        } else {
                            lessons.add(parseLesson(lesson, hours.get(i - 1), 0));

                            lessonGroup.put(day, lessons);
                        }
                    } catch (Exception ex) {
                        // Do nothing, no lesson that time
                    }
                }
            }
        }

        return lessonGroup;
    }

    /**
     * Should return lesson in this scheme:
     * lesson: [hour, lesson_name, group, room]
     * Used internally.
     *
     * @return list of lessons
     */
    private Lesson parseLesson(Element element, String hour, Integer num) {
        Lesson lesson = new Lesson();

        lesson.setName(getName(element, num));
        lesson.setTeacher(getTeacher(element, num));
        lesson.setRoom(getRoom(element, num));
        lesson.setGroup(getGroup(element, lesson.getName(), num));
        lesson.setHour(hour);

        // Correct up lesson name (remove group from it)
        lesson.setName(lesson.getName().replaceAll("(-|)" + lesson.getGroup(), ""));

        return lesson;
    }

    /**
     * Should return lesson's name or null.
     * Used internally.
     *
     * @return lesson's name or null
     */
    private String getName(Element element, int num) {
        String name = element.select(QUERY_SUBJECT).get(num).html();
        name = StringUtils.capitalize(name);
        name = StringUtils.trim(name);

        return name;
    }

    /**
     * Should return lesson's teacher or null.
     * Used internally.
     *
     * @return lesson's teacher or null
     */
    private String getTeacher(Element element, int num) {
        try {
            return element.select(QUERY_TEACHER).get(num).html();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Should return lesson's class room or null.
     * Used internally.
     *
     * @return lesson's class room or null
     */
    private String getRoom(Element element, int num) {
        try {
            return element.select(QUERY_ROOM).get(num).html();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Should return lesson's group or null.
     * Used internally.
     *
     * @return lesson's group or null
     */
    private String getGroup(Element element, String name, int num) {
        Matcher match = mRegexGroup.matcher(name);

        if (match.find()) {
            return match.group(0).substring(1);
        } else {
            for (String line : element.html().split("\n")) {
                match = mRegexGroup.matcher(line.trim());

                if (match.find()) {
                    return match.group(num).substring(1);
                }
            }
        }

        return null;
    }

}
