<script>
import { defineComponent, h} from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

export default defineComponent({
  name: 'ReactiveChart',
  components: {
    Bar
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

        return ()  => h(Bar, 
                  { chartData: props.chartData,
                    chartOptions: chartOptions})
  } 
})
</script>