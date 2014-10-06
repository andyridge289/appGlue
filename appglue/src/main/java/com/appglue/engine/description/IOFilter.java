package com.appglue.engine.description;

import java.util.ArrayList;

public class IOFilter {

    private long id;

    private ComponentService outputComponent;
    private ComponentService inputComponent;

    private ArrayList<IOValue> andFilters;
    private ArrayList<IOValue> orFilters;
}
