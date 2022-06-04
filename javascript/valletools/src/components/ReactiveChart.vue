<script>
import { defineComponent, h} from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
} from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale)

export default defineComponent({
  name: 'ReactiveChart',
  components: {
    Doughnut
  },
  props: {
         chartData:{
             default:[]
         }
      },
  setup(props) {
        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false
            }

        return ()  => h(Doughnut, 
                  { chartData: props.chartData,
                    chartOptions: chartOptions})
  } 
})
</script>