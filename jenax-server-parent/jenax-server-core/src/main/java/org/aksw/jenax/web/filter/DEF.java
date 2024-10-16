package org.aksw.jenax.web.filter;

import static org.apache.jena.riot.WebContent.contentTypeJSON;
import static org.apache.jena.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.riot.WebContent.contentTypeNQuads;
import static org.apache.jena.riot.WebContent.contentTypeNQuadsAlt1;
import static org.apache.jena.riot.WebContent.contentTypeNTriples;
import static org.apache.jena.riot.WebContent.contentTypeNTriplesAlt;
import static org.apache.jena.riot.WebContent.contentTypeRDFJSON;
import static org.apache.jena.riot.WebContent.contentTypeRDFThrift;
import static org.apache.jena.riot.WebContent.contentTypeRDFXML;
import static org.apache.jena.riot.WebContent.contentTypeResultsJSON;
import static org.apache.jena.riot.WebContent.contentTypeResultsThrift;
import static org.apache.jena.riot.WebContent.contentTypeResultsXML;
import static org.apache.jena.riot.WebContent.contentTypeTextCSV;
import static org.apache.jena.riot.WebContent.contentTypeTextPlain;
import static org.apache.jena.riot.WebContent.contentTypeTextTSV;
import static org.apache.jena.riot.WebContent.contentTypeTriG;
import static org.apache.jena.riot.WebContent.contentTypeTriGAlt1;
import static org.apache.jena.riot.WebContent.contentTypeTriX;
import static org.apache.jena.riot.WebContent.contentTypeTriXxml;
import static org.apache.jena.riot.WebContent.contentTypeTurtle;
import static org.apache.jena.riot.WebContent.contentTypeTurtleAlt1;
import static org.apache.jena.riot.WebContent.contentTypeXML;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Copy from org.apache.jena.fuseki - including all of fuseki causes all sorts of
 *  servlet API conflicts in our downstream projects (especially those involving apache spark) */

import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;

public class DEF
{
    public static final MediaType acceptRDFXML        = MediaType.create(contentTypeRDFXML);
    public static final MediaType acceptNQuads        = MediaType.create(contentTypeNQuads);
    public static final MediaType acceptRSXML         = MediaType.create(contentTypeResultsXML);
    public static final MediaType acceptJSON          = MediaType.create(contentTypeJSON);
    public static final MediaType acceptTurtle        = MediaType.create(contentTypeTurtle);

    public static final AcceptList jsonOffer          = AcceptList.create(contentTypeJSON);

    public static final AcceptList constructOffer     = AcceptList.create(contentTypeTurtle,
                                                                          contentTypeTurtleAlt1,
                                                                          contentTypeNTriples,
                                                                          contentTypeNTriplesAlt,
                                                                          contentTypeRDFXML,
                                                                          contentTypeTriX,
                                                                          contentTypeTriXxml,
                                                                          contentTypeJSONLD,
                                                                          contentTypeRDFJSON,
                                                                          contentTypeRDFThrift,

                                                                          contentTypeTriG,
                                                                          contentTypeTriGAlt1,
                                                                          contentTypeNQuads,
                                                                          contentTypeNQuadsAlt1
                                                                          );

    public static final AcceptList rdfOffer           = AcceptList.create(contentTypeTurtle,
                                                                          contentTypeTurtleAlt1,
                                                                          contentTypeNTriples,
                                                                          contentTypeNTriplesAlt,
                                                                          contentTypeRDFXML,
                                                                          contentTypeTriX,
                                                                          contentTypeTriXxml,
                                                                          contentTypeJSONLD,
                                                                          contentTypeRDFJSON,
                                                                          contentTypeRDFThrift
                                                                          );

    public static final AcceptList quadsOffer         = AcceptList.create(contentTypeTriG,
                                                                          contentTypeTriGAlt1,
                                                                          contentTypeJSONLD,
                                                                          contentTypeNQuads,
                                                                          contentTypeNQuadsAlt1,
                                                                          contentTypeTriX,
                                                                          contentTypeTriXxml
                                                                          );

    // Offer for SELECT
    // This include application/xml and application/json.
    public static final AcceptList rsOfferTable       = AcceptList.create(contentTypeResultsJSON,
                                                                          contentTypeJSON,
                                                                          contentTypeTextCSV,
                                                                          contentTypeTextTSV,
                                                                          contentTypeResultsXML,
                                                                          contentTypeXML,
                                                                          contentTypeResultsThrift,
                                                                          contentTypeTextPlain
                                                                          );

    // Offer for ASK
    // This includes application/xml and application/json and excludes application/sparql-results+thrift
    public static final AcceptList rsOfferBoolean      = AcceptList.create(contentTypeResultsJSON,
                                                                           contentTypeJSON,
                                                                           contentTypeTextCSV,
                                                                           contentTypeTextTSV,
                                                                           contentTypeResultsXML,
                                                                           contentTypeXML,
                                                                           contentTypeTextPlain
                                                                           );
}
