package picosoft.biz.arcep.client.kernel.intercomm;

import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.model.global.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;


@Service
public class StringResourceService {

    public static HashMap<String, String> arabe = new HashMap<String, String>();
    public static HashMap<String, String> francais = new HashMap<String, String>();
    public static HashMap<String, String> anglais = new HashMap<String, String>();

    private final Logger log = LoggerFactory.getLogger(StringResourceService.class);
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private KernelInterface kernelInterface;


    public String stringRessourceTrd(String data) {
        if (data == null) return (data);
        String l = null;
        if (currentUser.getStringResTrad() != null)
            l = currentUser.getStringResTrad();

        HashMap<String, String> defaultL = new HashMap<String, String>();
        if (l == null)
            defaultL = anglais;
        else if (l.equals(Language.AN))
            defaultL = anglais;
        else if (l.equals(Language.AR))
            defaultL = arabe;
        else if (l.equals(Language.FR))
            defaultL = francais;

        if (defaultL.containsKey(data))
            return defaultL.get(data);
        else return data;
    }



}
