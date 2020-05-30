package com.example.daggerapplication.services.printer.template;

import android.content.res.Resources;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.InputStream;


public class TemplateResourceLoader extends FileResourceLoader {

    private Resources resources;
    private String packageName;

    @Override
    public void commonInit(RuntimeServices rs, ExtendedProperties configuration) {
        super.commonInit(rs, configuration);
        this.resources = (Resources) rs.getProperty("android.content.res.Resources");
        this.packageName = (String) rs.getProperty("packageName");
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0L;
    }

    @Override
    public InputStream getResourceStream(String templateName) {
        int id = resources.getIdentifier(templateName, "raw", this.packageName);
        return resources.openRawResource(id);
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public boolean resourceExists(String templateName) {
        return resources.getIdentifier(templateName, "raw", this.packageName) != 0;
    }
}