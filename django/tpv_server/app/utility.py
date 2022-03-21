# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-21T02:39:46+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-21T02:40:03+01:00
# @License: Apache License v2.0


def componentToHex(c):
  str_hex = hex(int(c)).replace("0x","")
  return '0' + str_hex if len(str_hex) == 1 else str_hex;


def rgbToHex(color):
    colorComponents = color.split(",")
    r = colorComponents[0]
    g = colorComponents[1]
    b = colorComponents[2]
    str_hex = "#" + componentToHex(r) + componentToHex(g) + componentToHex(b)
    return str_hex
