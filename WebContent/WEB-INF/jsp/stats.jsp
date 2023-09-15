<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="nl">
<head>
	<%@include file="_head.jsp" %>		
</head>

<body data-spy="scroll" data-target="#navbar-example">

	<%@include file="_header.jsp"%>
		
	<div class="top-space"></div>
	
	<!-- container -->
	<div class="container">

		<br/><br/>

		<div class="row">
			<div class="col-md-6">
				<canvas id="canRanking"></canvas>
			</div>
			<div class="col-md-6">
				<div class="row">
					<div class="col-md-12">
						<canvas id="canRadar"></canvas>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="top-space"></div>
						<canvas id="canHistoricalCustomerSatisfaction"></canvas>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="top-space"></div>
						<canvas id="canTotalCost"></canvas>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="top-space"></div>
						<canvas id="canThroughputTime"></canvas>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="top-space"></div>
						<canvas id="canServiceTime"></canvas>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="top-space"></div>
						<canvas id="canWaitingTime"></canvas>
					</div>
				</div>
			</div>
		</div>			

	</div>	<!-- /container -->	
	
	<div class="top-space"></div>
	<div class="top-space"></div>
	
	<%@include file="_footer.jsp" %>
  	<script src="/assets/js/moment.min.js"></script>
  	<script src="/assets/js/Chart.min.js"></script>
	<script>
	window.chartColors = {
			red: 'rgb(255, 99, 132)',
			orange: 'rgb(255, 159, 64)',
			yellow: 'rgb(255, 205, 86)',
			green: 'rgb(75, 192, 192)',
			blue: 'rgb(54, 162, 235)',
			purple: 'rgb(153, 102, 255)',
			grey: 'rgb(201, 203, 207)',
			cyan: 'rgb(62, 193, 213)',
		};
	var color = Chart.helpers.color;
	
	var ctxRanking = document.getElementById("canRanking").getContext('2d');
	var crtRanking = new Chart(ctxRanking, {
		type: 'horizontalBar',
		data: {
			labels: JSON.parse('${groups}'),
			datasets: [{
				label: 'Own score',
				backgroundColor: color(window.chartColors.cyan).alpha(0.5).rgbString(),
				borderColor: window.chartColors.cyan,
				borderWidth: 1,
				data: JSON.parse('${ownScore}')
			},
			{
				label: 'Others score',
				backgroundColor: color(window.chartColors.grey).alpha(0.5).rgbString(),
				borderColor: window.chartColors.grey,
				borderWidth: 1,
				data: JSON.parse('${othersScore}')
			}]
		},
		options: {
			elements: {
				rectangle: {
					borderWidth: 2,
				}
			},
			tooltips: {
				enabled: false,
			},
			responsive: true,
			maintainAspectRatio: false,
			legend: {
				display: false
			},
			title: {
				display: true,
				text: 'Ranking'
			},
			scales: {
				xAxes: [{
					display: false,
					stacked: true,
				}],
				yAxes: [{
					stacked: true
				}]
			}
		}
	});
	crtRanking.canvas.parentNode.style.height = (${groups}.length * 25) + 'px';

	function round(value, decimals) {
		return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
	}
	
	function createData(dates, values){
		var data = [];
		for (i = 0; i < dates.length; i++){
			data.push({'x': moment.utc(dates[i]), 'y': round(values[i],1)});
		}
		return data;
	}
	
	function createHistoricalGraph(data, canvas, title, unit){
		var context = document.getElementById(canvas).getContext('2d');
		var chart = new Chart(context, {
			type: 'line',
			data: {
				datasets: [{
					label: unit,
					backgroundColor: color(window.chartColors.cyan).alpha(0.5).rgbString(),
					borderColor: window.chartColors.cyan,
					borderWidth: 1,
					data: data
				}]
			},
			options: {
				responsive: true,
				legend: {
					display: false
				},
				title: {
					display: true,
					text: title
				},
				scales: {
					xAxes: [{
						type: 'time',
						time: {
							unit: 'day',
							displayFormat: { 
								day: 'DD MMM'
							},
							tooltipFormat: 'DD MMM YYYY'
						}, 
						scaleLabel: {
							display: false,
						}
					}],
					yAxes: [{
			            ticks: {
			            	beginAtZero: true
						},
						scaleLabel: {
							display: true,
							labelString: unit
						}
					}]
				},
			}
		});
	}
	
	createHistoricalGraph(createData(JSON.parse('${dates}'),JSON.parse('${customerSatisfaction}')), 'canHistoricalCustomerSatisfaction', 'Customer Satisfaction History', 'customer satisfaction');
	createHistoricalGraph(createData(JSON.parse('${dates}'),JSON.parse('${totalCost}')), 'canTotalCost', 'Total Cost History', 'cost (euro)');
	createHistoricalGraph(createData(JSON.parse('${dates}'),JSON.parse('${throughputTime}')), 'canThroughputTime', 'Average Throughput Time History', 'time (hours)');
	createHistoricalGraph(createData(JSON.parse('${dates}'),JSON.parse('${serviceTime}')), 'canServiceTime', 'Average Service Time History', 'time (hours)');
	createHistoricalGraph(createData(JSON.parse('${dates}'),JSON.parse('${waitingTime}')), 'canWaitingTime', 'Average Waiting Time History', 'time (hours)');
	
	var ctxRadar = document.getElementById("canRadar").getContext('2d');
	var crtRadar = new Chart(ctxRadar, {
		type: 'radar',
		data: {
			labels: JSON.parse('${radarDimensions}'),
			datasets: [{
				label: 'Own score',
				backgroundColor: color(window.chartColors.cyan).alpha(0.5).rgbString(),
				borderColor: window.chartColors.cyan,
				borderWidth: 1,
				data: JSON.parse('${radarSelf}').map(function (e){return round(e*100,0);})
			},
			{
				label: 'Best group score',
				backgroundColor: color(window.chartColors.grey).alpha(0.5).rgbString(),
				borderColor: window.chartColors.grey,
				borderWidth: 1,
				data: JSON.parse('${radarBest}').map(function (e){return round(e*100,0);})
			}]
		},
		options: {
			maintainAspectRatio: true,
			spanGaps: false,
			elements: {
				line: {
					tension: 0.000001
				}
			},
			plugins: {
				filler: {
					propagate: false
				}
			},
			scale: {
				ticks: {
                	beginAtZero: true,
                    steps: 10,
                    stepValue: 10,
                    max: 100
				}
            },
            title: {
                display: true,
                text: 'Comparative performance (% of best)'
            }
		}
	});
	</script>
  </body>
</html>