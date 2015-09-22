/*
 * Copyright 2015 AlexS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fenvariel.mavenfreemarker;

import freemarker.template.Configuration;

public enum Version {

    VERSION_2_3_0(Configuration.VERSION_2_3_0),
    VERSION_2_3_19(Configuration.VERSION_2_3_19),
    VERSION_2_3_20(Configuration.VERSION_2_3_20),
    VERSION_2_3_21(Configuration.VERSION_2_3_21),
    VERSION_2_3_22(Configuration.VERSION_2_3_22),
    VERSION_2_3_23(Configuration.VERSION_2_3_23);

    public final freemarker.template.Version freemarkerVersion;

    private Version(freemarker.template.Version freemarkerVersion) {
        this.freemarkerVersion = freemarkerVersion;
    }

}
