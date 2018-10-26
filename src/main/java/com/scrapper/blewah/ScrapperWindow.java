package com.scrapper.blewah;

import org.apache.poi.ss.usermodel.Cell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ScrapperWindow implements ActionListener {
    public JButton buttonGo;
    public JButton buttonAdd;
    public JFrame frame;


    public static void main(String[] args){
        //scrapeTopic("anime/fairy-tail/");

        ScrapperWindow obj = new ScrapperWindow();
        obj.prepareGUI();

//        testScrap("http://blewaah.com/one-piece-episode-786-subtitle-indonesia/?tabgarb=tab3");
        //scrapeAnime("http://blewaah.com/anime/another/");
    }
//    public static void testScrap(String url){
//        String html = getUrl(url);
//        Document doc = Jsoup.parse(html);
//
//        String coba = "";
//
//        System.out.println(coba);
//
//    }

    public static String getUrl(String url) {
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("The url was malformed!");
            return "";
        }

        URLConnection urlCon = null;
        BufferedReader in = null;
        String outputText = "";

        try {
            urlCon = urlObj.openConnection();
            in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                outputText += line;
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("There was an error connecting to the URL");
            return "";
        }
        return outputText;
    }

    public static Document getUrlJsoup(String url){
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("The url was malformed!");
            return null;
        }
        Document output = null;
        try {
            output = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36").get();
        } catch (IOException e){
            System.out.println(e);
            System.out.println("There was an error connecting to the URL");
        }

        return output;
    }

    public static Document processAnime(String url){
        Document doc = getUrlJsoup(url);
        return doc;
    }

    public static String getLink_autovid(Document doc){
        Element link = doc.select("#page > main > section > article > div.tabgarb_content > div > div > div > div > video > source").first();

        String linkStr = "";
        linkStr = link.attr("src");
        return linkStr;

    }

    public static String getLink_fvideo(Document doc){
        Element link = doc.select("#page > main > section > article > div.tabgarb_content > div > div > div").first();

        String linkStr = "";
        linkStr = link.attr("data-item");
        linkStr = linkStr.substring(20, linkStr.length() - 24).replaceAll("\\\\","");
        return linkStr;
    }

    public static void scrapeAnime(String url, String slug){
        Document doc = getUrlJsoup(url);
        Elements links = doc.select("#main > section > table > tbody [class~=_col-eps]");
        String animTitleStr = slug;


        List<String> titleList = new ArrayList<String>();
        List<String> linkList = new ArrayList<String>();
        List<Integer> epsList = new ArrayList<Integer>();
        int eps = links.size();

        for (Element linkAndTitle: links) {
            String title = linkAndTitle.getElementsByClass("eps").first().attr("title");
            String link = linkAndTitle.getElementsByClass("eps").first().attr("href") + "?tabgarb=tab1";

            doc = processAnime(link);
            String videoLink = "";
            try{
                Element tabType = doc.select("#tabgarb > li.tabgarbactive > a").first();
                if (tabType != null){
                    String check_title = tabType.attr("title");
                    if (check_title.equals("Autovid") || check_title.equals("Video") || check_title.equals("AutoVid")){
                        videoLink = getLink_autovid(doc);
                    }else if(check_title.equals("Fvideo")){
                        videoLink = getLink_fvideo(doc);
                    }else{
                        Elements allTab = doc.select("#tabgarb > li > a");
                        String link_fix = "";
                        String title_get = "";
                        for (Element tab: allTab){
                            check_title = tab.attr("title");
                            if (check_title.equals("Autovid") || check_title.equals("Fvideo") || check_title.equals("AutoVid") || check_title.equals("Video")){
                                link_fix = tab.attr("href");
                                title_get = check_title;
                                break;
                            }
                        }

                        if (!link_fix.equals("")){
                            doc = getUrlJsoup(link_fix);
                            if(title_get.equals("Autovid") || title_get.equals("Video") || check_title.equals("AutoVid")){
                                videoLink = getLink_autovid(doc);
                            }else if (title_get.equals("Fvideo")){
                                videoLink = getLink_fvideo(doc);
                            }
                        }
                    }
                }
            }
            catch(Exception e){
                System.out.println("ERROR ON :" + title);
            }


            titleList.add(title);
            linkList.add(videoLink);
            epsList.add(eps);
            eps-=1;
            System.out.println("title: "+ title + "|episode: " + eps +"|link: " + videoLink);
        }
        createAndInsertAnimeSheet(animTitleStr, titleList,linkList);
    }

    public static void createAndInsertAnimeSheet(String mainTitle, List<String> titles, List<String> links){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Anime and Link");
        XSSFRow row;
        Map <Integer, Object[]> anime_link = new TreeMap <Integer, Object[]>(
                new Comparator<Integer>() {

                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o2.compareTo(o1);
                    }

                });

        for(int i = 0; i<links.size(); i++){
            anime_link.put( i, new Object[] { titles.get(i), links.get(i)});
        }


        Set <Integer> keyid = anime_link.keySet();
        int rowid = 0;

        for (Integer key : keyid) {
            row = spreadsheet.createRow(rowid++);
            Object [] objectArr = anime_link.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String)obj);
            }
        }

        mainTitle = mainTitle.replaceAll("/", "_");
        mainTitle = mainTitle.replaceAll(" ", "_");
        File test = new File(mainTitle+".xlsx");
        FileOutputStream out;
        try{
            out = new FileOutputStream(test);
            workbook.write(out);
            Desktop.getDesktop().open(test);
            out.close();
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public void prepareGUI(){
        frame = new JFrame("Blewah ScrapperWindow");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700,150);

        ArrayList<String> list_val = new ArrayList<String>();
        ArrayList<String> list_slug = new ArrayList<String>();
        list_val.add("");
        list_slug.add("");
        this.addLinkForm(frame.getContentPane(), list_val, list_slug);

//        JTextField linkscrape = new JTextField();
//        //linkscrape.setBounds(128, 28, 86, 20);
//        frame.getContentPane().add(linkscrape);
//
//        frame.getContentPane().add(button);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == buttonAdd){
            ArrayList<String> list_val = new ArrayList<String>();
            ArrayList<String> list_slug = new ArrayList<String>();
            for (Component component : frame.getContentPane().getComponents()) {
                if (component instanceof JTextField) {
                    if (component.getName().equals("link")){
                        list_val.add(((JTextField) component).getText());
                    }else if(component.getName().equals("slug")){
                        list_slug.add(((JTextField) component).getText());
                    }
                }
            }
            list_val.add("");
            list_slug.add("");
            this.addLinkForm(frame.getContentPane(), list_val, list_slug);

        }else if (e.getSource() == buttonGo){
            ArrayList<String> list_val = new ArrayList<String>();
            ArrayList<String> list_slug = new ArrayList<String>();
            for (Component component : frame.getContentPane().getComponents()){
                if (component instanceof JTextField) {
                    if (component.getName().equals("link")){
                        list_val.add(((JTextField) component).getText());
                    }else if(component.getName().equals("slug")){
                        list_slug.add(((JTextField) component).getText());
                    }
                }
            }
            if (list_slug.size() != list_val.size()){
                JOptionPane.showMessageDialog(frame,
                        "slug or link didn't correctly set",
                        "Input warning",
                        JOptionPane.WARNING_MESSAGE);
            }
            for (int i=0; i<list_val.size(); i++ ){
                if (!list_val.get(i).equals("") && !list_slug.get(i).equals("")){
                    scrapeAnime(list_val.get(i),list_slug.get(i));
                }else if(!list_val.get(i).equals("") && list_slug.get(i).equals("")){
                    JOptionPane.showMessageDialog(frame,
                            "slug can't be Empty for " + list_val.get(i),
                            "Input warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }

        }

    }

    public void addLinkForm(Container container, ArrayList<String> list_value, ArrayList<String> list_slug){
        container.removeAll();

        GridBagLayout layout = new GridBagLayout();
        container.setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();

        int iter = Math.max(list_value.size(), list_slug.size());

        for (int j=0; j<iter; j++) {
            JLabel label = new JLabel("Link:");
            Insets i = new Insets(1, 10, 1, 1);
            gbc.insets = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = j;
            gbc.weightx = 0.05;
            container.add(label, gbc);

            JTextField linkField = new JTextField();
            linkField.setName("link");
            linkField.setText(list_value.get(j));
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.gridy = j;
            gbc.weightx = 2;
            container.add(linkField, gbc);

            JLabel slugLabel = new JLabel("Slug:");
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 2;
            gbc.gridy = j;
            gbc.weightx = 0.05;
            container.add(slugLabel, gbc);

            JTextField slugField = new JTextField();
            slugField.setName("slug");
            slugField.setText(list_slug.get(j));
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 3;
            gbc.gridy = j;
            gbc.weightx = 0.75;
            container.add(slugField, gbc);
        }
        int y = iter;

        this.buttonAdd = new JButton("+ Link");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Insets i = new Insets(1, 3, 1, 10);
        gbc.insets = i;
        gbc.weightx = 0.2;
        gbc.gridx = 4;
        gbc.gridy = y - 1;
        frame.getContentPane().add(buttonAdd, gbc);

        buttonAdd.addActionListener(this);

        this.buttonGo = new JButton("Go");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        i = new Insets(1, 3, 1, 10);
        gbc.insets = i;
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = y;
        frame.getContentPane().add(buttonGo, gbc);

        buttonGo.addActionListener(this);

        container.validate();
        container.repaint();
    }


}




