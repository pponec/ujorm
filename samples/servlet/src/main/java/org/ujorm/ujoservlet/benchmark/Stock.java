/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.benchmark;

/**
 *
 * @author Pavel Ponec
 */
public class Stock {

    private String name;

    private String name2;

    private String url;

    private String symbol;

    private double price;

    private double change;

    private double ratio;

    public Stock(String name, String name2, String url, String symbol, double price, double change, double ratio) {
        this.name = name;
        this.name2 = name2;
        this.url = url;
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.ratio = ratio;
    }

    public String getName() {
        return this.name;
    }

    public String getName2() {
        return this.name2;
    }

    public String getUrl() {
        return this.url;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public double getPrice() {
        return this.price;
    }

    public double getChange() {
        return this.change;
    }

    public double getRatio() {
        return this.ratio;
    }

}
