package service;

import data.CommunityDao;
import model.Community;

import java.util.ArrayList;

/*
 * Thin layer over CommunityDao. Mostly it just passes calls through, but it
 * also knows the list of categories we show on the discover screen.
 */
public class CommunityService {

    private CommunityDao dao = new CommunityDao();

    // the categories we group communities under (used by discover). the actual
    // list lives in model.Categories so both sides can see it.
    public static final String[] CATEGORIES = model.Categories.ALL;

    public ArrayList<Community> getAll() {
        return dao.getAll();
    }

    public Community findById(int id) {
        return dao.findById(id);
    }

    public ArrayList<Community> getByCategory(String category) {
        return dao.getByCategory(category);
    }

    public ArrayList<Community> search(String text) {
        return dao.search(text);
    }

    public ArrayList<Community> getJoined(String username) {
        return dao.getJoined(username);
    }

    public boolean isMember(String username, int communityId) {
        return dao.isMember(username, communityId);
    }

    public void join(String username, int communityId) {
        dao.join(username, communityId);
    }

    public void leave(String username, int communityId) {
        dao.leave(username, communityId);
    }
}
