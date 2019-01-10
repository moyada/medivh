# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

> A simple, automatic, and flexible method parameter check library for the Java platform.

## Documentation

* [Getting Started](https://moyada.github.io/medivh/)

## Motivation

We often need to check the method parameters, especially in remote invocation.
This library can save time and effort in this respect, modify the syntax tree at `compilation phase` by configure annotations, and add parameter validation to method.

## Features

* Check whether an object is null.

* Check the size range of basic numeric type (such as int and Integer).

* Check whether a string is blank.

* Check the length range of String or array.

* Check the capacity range of Collection or Map.

* Throw an exception or return data when validated is fails.

## Preview

```
public class MyApp {

    @Throw
    public Boolean run(Args args,
                    @Return("null") @NotBlank String name,
                    @Return("false") @Min(1) int num) {
        System.out.println("process");
        return true;
    }

    class Args {

        @Max(1000) int id;

        @NotBlank String name;

        @Nullable @DecimalMin(-25.02) @DecimalMax(200) Double price;

        @Size(min = 10, max = 10) String[] values;

        @NotNull HashMap<String, Object> param;

        @Size(max = 5) List<String> extra;
    }
}
```

After compilation, the program will be this.

```
public class MyApp {
    public MyApp() {
    }

    public Boolean run(MyApp.Args args, String name, int num) {
        if (args == null) {
            throw new IllegalArgumentException("Invalid input parameter, cause args is null");
        } else {
            String mvar_0 = args.invalid0();
            if (mvar_0 != null) {
                throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
            } else if (name == null) {
                return null;
            } else if (isBlank(name)) {
                return null;
            } else if (num < 1) {
                return false;
            } else {
                System.out.println("process");
                return true;
            }
        }
    }

    class Args {
        int id;
        String name;
        Double price;
        String[] values;
        HashMap<String, Object> param;
        List<String> extra;

        Args() {
        }

        public String invalid0() {
            if (this.id > 1000) {
                return "id great than 1000";
            } else if (this.values == null) {
                return "values is null";
            } else if (this.values.length != 10) {
                return "values cannot equals 10";
            } else if (this.param == null) {
                return "param is null";
            } else {
                if (this.price != null) {
                    if (this.price > 200.0D) {
                        return "price great than 200.0";
                    }

                    if (this.price < -25.02D) {
                        return "price less than -25.02";
                    }
                }

                if (this.name == null) {
                    return "name is null";
                } else if (isBlank(this.name)) {
                    return "name is blank";
                } else if (this.extra == null) {
                    return "extra is null";
                } else {
                    return this.extra.size() > 5 ? "extra.size() great than 5" : null;
                }
            }
        }
    }
    
    public static boolean isBlank(String str) {
        int length = str.length();
        if (length == 0) {
            return true;
        } else {
            for(int i = 0; i < length; ++i) {
                char ch = str.charAt(i);
                if (ch != ' ') {
                    return false;
                }
            }
    
            return true;
        }
    }
}
```