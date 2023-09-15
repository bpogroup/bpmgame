<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="nl">
<head>
<%@include file="_head.jsp"%>
<link href="/assets/css/bootstrap-datetimepicker.min.css" rel="stylesheet">
</head>

<body data-spy="scroll" data-target="#navbar-example">

	<%@include file="_header.jsp"%>

	<div class="top-space"></div>

	<!-- container -->
	<div class="container">

		<br />
		<br />

		<div class="row">
			<div class="panel panel-default col-md-12">
  				<div class="panel-body">
  					<form method="post" action="${applicationScope.constants.modelCases}">
						<div class="row">
							<div class="col-md-6">
								<div class="form-group row">
									<label class="col-md-6" style="padding-top:3pt">Showing situation at moment:</label>
		        	        		<div class='input-group date col-md-6' id='dtpicker'>
		            	        		<input name="datetime" id="datetime" type='text' class="form-control" value="${datetime}"/>
		                	    		<span class="input-group-addon">
		                    	    		<span class="glyphicon glyphicon-calendar"></span>
		                    			</span>
			            			</div>
			            		</div>
			            	</div>
			            	<div class="col-md-1">&nbsp;</div>
			            	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
			            	<div class="col-md-3">
			            		<button type="submit" class="btn btn-primary">Select moment</button>
			            	</div>
			            </div>
		            </form>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6">
				<h3 class="thin text-center">Active tasks</h3>

				<table class="table table-striped">
					<thead>
						<tr>
							<th>Task</th>
							<th>Case</th>
							<th>Resource</th>
							<th>Active (minutes)</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${taskStates}" var="t">
							<tr>
								<td><c:out value="${t.task}" /></td>
								<td><c:out value="${t.caseid}" /></td>
								<td><c:out value="${t.resource}" /></td>
								<td><c:out value="${t.active}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>

			</div>
			<div class="col-md-6">
				<h3 class="thin text-center">Today's employee load</h3>
				
				<canvas id="canEmployeeTimes"></canvas>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6">
				<h3 class="thin text-center">Today's cases</h3>
				
				<canvas id="canCaseStates"></canvas>
			
				<br/>
				
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Case</th>
							<th>Busy</th>
							<th>Idle</th>
							<th>Complete</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${caseStates}" var="c">
							<tr>
								<td><c:out value="${c.caseid}" /></td>
								<td><c:out value="${c.busy}" /></td>
								<td><c:out value="${c.idle}" /></td>
								<td><c:out value="${c.complete}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>

			</div>

		</div>

	</div>
	<!-- /container -->

	<div class="top-space"></div>
	<div class="top-space"></div>

	<%@include file="_footer.jsp"%>
	<script src="/assets/js/moment.min.js"></script>
  	<script src="/assets/js/Chart.min.js"></script>
  	<script src="/assets/js/bootstrap-datetimepicker.min.js"></script>
	<script>
    $(function () {
        $('#dtpicker').datetimepicker({
        	format: 'YYYY/MM/DD HH:mm'
        });
    });
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
	
	var ctxEmployeeTimes = document.getElementById("canEmployeeTimes").getContext('2d');
	var crtEmployeeTimes = new Chart(ctxEmployeeTimes, {
		type: 'horizontalBar',
		data: {
			labels: JSON.parse('${employeeNames}'),
			datasets: [{
				label: '',
				backgroundColor: color(window.chartColors.cyan).alpha(0.5).rgbString(),
				borderColor: window.chartColors.cyan,
				borderWidth: 1,
				data: JSON.parse('${employeeTimes}').map(function (e) {return e/60000;})
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
			legend: {
				display: false
			},
			title: {
				display: true,
				text: 'Employee active time (minutes)'
			},
			scales: {
				xAxes: [{
					display: true,
				}],
				yAxes: [{
					stacked: true
				}]
			}
		}
	});
	
	var ctxCaseStates = document.getElementById("canCaseStates").getContext('2d');
	var crtCaseStates = new Chart(ctxCaseStates, {
			type: 'doughnut',
			data: {
				datasets: [{
					data: [
						${busyCases},
						${idleCases},
						${completedCases}
					],
					backgroundColor: [
						color(window.chartColors.red).alpha(0.5).rgbString(),
						color(window.chartColors.purple).alpha(0.5).rgbString(),
						color(window.chartColors.cyan).alpha(0.5).rgbString()
					],
					label: ''
				}],
				labels: [
					'Busy',
					'Idle',
					'Completed'
				]
			},
			options: {
				responsive: true,
				legend: {
					position: 'bottom',
				},
				title: {
					display: false,
				},
				animation: {
					animateScale: true,
					animateRotate: true
				}
			}
		});
	
	</script>
</body>
</html>