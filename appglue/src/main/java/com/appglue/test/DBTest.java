package com.appglue.test;

import android.test.AndroidTestCase;
import android.util.Log;

import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.ServiceFactory;

import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.TAG;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class DBTest extends AndroidTestCase {

    public void testServiceDescriptions() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        ServiceDescription helloService = getService(registry, "setupHelloService");

        registry.addServiceDescription(helloService);
        ServiceDescription helloService2 = registry.getAtomic(helloService.className());

        if(helloService.equals(helloService2))
            assertEquals(true, true);
        else
            assertEquals(helloService, helloService2);
    }

    private ServiceDescription getService(Registry registry, String name)throws Exception {

        ServiceFactory sf = ServiceFactory.getInstance(registry, getContext());

        Method helloMethod = sf.getClass().getDeclaredMethod(name);
        helloMethod.setAccessible(true);
        String helloServiceString = (String) helloMethod.invoke(sf);

        JSONObject service = new JSONObject(helloServiceString).getJSONObject(JSON_SERVICE).getJSONObject(JSON_SERVICE_DATA);
        AppDescription app = sf.getAppDescription();

        return ServiceDescription.parseFromNewJSON(service, app);
    }

    public void testAComposite() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        ServiceDescription tubeService = getService(registry, "setupTubeService");
        ServiceDescription notificationService = getService(registry, "setupNotificationService");
    }
}
