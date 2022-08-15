/**
 * @Author: Manuel Rodriguez <valle>
 * @Date:   2019-02-21T01:58:24+01:00
 * @Email:  valle.mrv@gmail.com
 * @Last modified by:   valle
 * @Last modified time: 2019-02-26T09:43:05+01:00
 * @License: Apache License v2.0
 */

function on_click(value, widget_id){
 $(".row-select").removeClass("row-selected");
 $("."+value).addClass("row-selected")
 $("#"+widget_id).val(convertColor(value.replace("_","")));
}

function componentToHex(c) {
  var hex = c.toString(16);
  return hex.length == 1 ? "0" + hex : hex;
}

function rgbToHex(color) {
    var colorComponents = color.split(",");
    var r = parseInt(colorComponents[0]);
    var g = parseInt(colorComponents[1]);
    var b = parseInt(colorComponents[2]);
    var hex = "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
    return hex;
}

function convertColor(color) {
  /* Check for # infront of the value, if it's there, strip it */

  if(color.substring(0,1) == '#') {
     color = color.substring(1);
   }

  var rgbColor = {};

  /* Grab each pair (channel) of hex values and parse them to ints using hexadecimal decoding */
  rgbColor.rChannel = parseInt(color.substring(0,2),16);
  rgbColor.gChannel = parseInt(color.substring(2,4),16);
  rgbColor.bChannel = parseInt(color.substring(4),16);

  return "" +rgbColor.rChannel+","+rgbColor.gChannel+","+rgbColor.bChannel;
 }
