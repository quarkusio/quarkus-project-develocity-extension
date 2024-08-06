package io.quarkus.develocity.project.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public final class MavenMojoExecutionConfig {

    private final MojoExecution mojoExecution;

    public MavenMojoExecutionConfig(MojoExecution mojoExecution) {
        this.mojoExecution = mojoExecution;
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean( getString( key ) );
    }

    public String getString(String key) {
        var configElement = mojoExecution.getConfiguration().getChild( key );
        if ( configElement == null ) {
            return null;
        }
        var value = configElement.getValue();
        if ( value == null ) {
            return null;
        }
        return value.trim();
    }

    public List<String> getStringList(String key) {
        var configElement = mojoExecution.getConfiguration().getChild( key );
        if ( configElement == null ) {
            return List.of();
        }
        List<String> children = new ArrayList<>();
        for ( Xpp3Dom configElementChild : configElement.getChildren() ) {
            var value = configElementChild.getValue();
            if ( value != null ) {
                children.add( value );
            }
        }
        return children;
    }

    public Map<String, String> getStringMap(String key) {
        var configElement = mojoExecution.getConfiguration()
                .getChild( key );
        if ( configElement == null ) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for ( Xpp3Dom configElementChild : configElement.getChildren() ) {
            result.put( configElementChild.getName(), configElementChild.getValue() );
        }
        return result;
    }
}
