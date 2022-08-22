from django.db.models import Sum
from datetime import datetime
from gestion.models import Lineaspedido

def get_total_by_horas(filter):
    res = []
    data = {}
    time_last = datetime.strptime("00:00","%H:%M")
    for l in Lineaspedido.objects.filter(**filter).values_list(
              "infmesa__hora").annotate(total=Sum("precio")):
        
        time_self = datetime.strptime(l[0],"%H:%M")
        if (time_self.hour == time_last.hour):
            data["total"] = data["total"] + l[1]

        else:
            time_last = time_self
            data = {"hora": str(time_self.hour)+":00", "total": l[1]}
            res.append(data)
    return res


def get_total(order_by, filter):
    res = []
    for l in Lineaspedido.objects.filter(**filter).values_list(order_by).annotate(total=Sum("precio")):
        res.append({"estado":l[0], "total": float(l[1])})
    return res