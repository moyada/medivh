# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

A simple, automatic, and flexible method parameter check library for the Java platform.

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

## Requirements

JDK 1.6 or higher.

JDK 1.8 or higher if validate target is an interface. 