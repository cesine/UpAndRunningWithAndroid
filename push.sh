#!/bin/bash

hg pull ;
hg update ;

hg merge ;
hg commit -m "$1" ;

hg push ;

