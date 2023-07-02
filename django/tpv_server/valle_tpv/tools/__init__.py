# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-21T23:27:08+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T01:13:44+02:00
# @License: Apache License v2.0

def is_float(str):
    try:
        float(str)
        return True
    except:
        return False