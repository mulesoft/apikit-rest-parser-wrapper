/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.ProfileName;
import amf.client.parse.Parser;
import amf.client.resolve.Resolver;

public class AMFParserWrapper {

    private Parser parser;
    private Resolver resolver;
    private ProfileName profileName;

    public AMFParserWrapper(Parser parser, Resolver resolver, ProfileName profileName) {
        this.parser = parser;
        this.resolver = resolver;
        this.profileName = profileName;
    }

    public Parser getParser() {
        return parser;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public ProfileName getProfileName() {
        return profileName;
    }
}
