package com.bilibli.local.city.stat.privacy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PrivacyController {

    @Autowired
    private PrivacyDAO privacyDAO;

    @GetMapping("test1")
    public Object test1() {
        return "test1";
    }

    @GetMapping("test")
    public Object test() {
        int offset = 0;
        List<PrivacyDO> data = privacyDAO.getPrivacy(offset);
        Map<Integer, Integer> privacy_off_lc_off = new HashMap<>();
        Map<Integer, Integer> privacy_off_lc_on = new HashMap<>();
        Map<Integer, Integer> privacy_on_lc_on = new HashMap<>();
        Map<Integer, Integer> privacy_on_lc_off = new HashMap<>();
        while (data.size() > 0) {
            for (PrivacyDO privacyDO : data) {
                offset = privacyDO.id;
                String table = String.format("t_city_switch_%d", privacyDO.uid % 10);
                UserDO userDO = privacyDAO.getUserDO(table, privacyDO.uid);
                if (userDO == null) {
                    continue;
                }
                if (privacyDO.dyn_status == 1) {
                    if (userDO.is_open) {
                        if (privacy_on_lc_on.containsKey(userDO.city)) {
                            privacy_on_lc_on.put(userDO.city, privacy_on_lc_on.get(userDO.city) + 1);
                        } else {
                            privacy_on_lc_on.put(userDO.city, 1);
                        }
                    } else {
                        if (privacy_on_lc_off.containsKey(userDO.city)) {
                            privacy_on_lc_off.put(userDO.city, privacy_on_lc_off.get(userDO.city) + 1);
                        } else {
                            privacy_on_lc_off.put(userDO.city, 1);
                        }
                    }
                } else {
                    if (userDO.is_open) {
                        if (privacy_off_lc_on.containsKey(userDO.city)) {
                            privacy_off_lc_on.put(userDO.city, privacy_off_lc_on.get(userDO.city) + 1);
                        } else {
                            privacy_off_lc_on.put(userDO.city, 1);
                        }
                    } else {
                        if (privacy_off_lc_off.containsKey(userDO.city)) {
                            privacy_off_lc_off.put(userDO.city, privacy_off_lc_off.get(userDO.city) + 1);
                        } else {
                            privacy_off_lc_off.put(userDO.city, 1);
                        }
                    }
                }
            }
            data = privacyDAO.getPrivacy(offset);
        }
        Map<String, Map> result = new HashMap<>();
        result.put("privacy_off_lc_off", privacy_off_lc_off);
        result.put("privacy_off_lc_on", privacy_off_lc_on);
        result.put("privacy_on_lc_off", privacy_on_lc_off);
        result.put("privacy_on_lc_on", privacy_on_lc_on);
        return result;
    }
}
