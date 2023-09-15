<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="nl">
<head>
<%@include file="_head.jsp"%>
</head>

<body data-spy="scroll" data-target="#navbar-example">

	<%@include file="_header.jsp"%>

	<div class="top-space"></div>

	<!-- container -->
	<div class="container">
		<br/><br/>
		<div class="row">
			<article class="col-xs-12 maincontent">
				<div class="col-md-10 col-md-offset-1 col-sm-10 col-sm-offset-1">
					<div class="panel panel-default">
						<div class="panel-body">
							<h3 class="thin text-center">Models</h3>
							<hr>
							<form id="myform" method="post" data-toggle="validator"
								role="form" action="${applicationScope.constants.modelUpload}?${_csrf.parameterName}=${_csrf.token}"
								enctype="multipart/form-data">
								<div id="fileUploadBlock" class="form-group">
									<label for="file" class="control-label">Model <span class="text-danger">*</span></label>
									<div class="input-group">
										<span class="input-group-btn"> 
											<span class="btn btn-primary btn-file"> 
												Browse&hellip; <input id="file" name="file" type="file" class="form-control">
											</span>
										</span>
										<input id="fileText" type="text" class="form-control" required>
									</div>
									<div class="help-block with-errors"></div>
								</div>

								<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

								<div class="form-group">
									<button class="btn btn-action" type="submit">Deploy model</button>
								</div>
							</form>
						</div>
					</div>
				</div>
			</article>
		</div>
		
		<div class="row">
			<article class="col-xs-12 maincontent">
				<div class="col-md-10 col-md-offset-1 col-sm-10 col-sm-offset-1">
					<div class="panel panel-default">
						<div class="panel-body">		
							<table class="table table-striped">
								<thead>
									<tr>
										<th>Model</th>
										<th>Deployed on</th>
										<th>Deployed by</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${models}" var="m">
										<tr>
   											<td><c:out value="${m.fileName}" /></td>
				   							<td><fmt:formatDate pattern="yyyy-MM-dd  HH:mm" value="${m.deployedOn}" /></td>
   											<td><c:out value="${m.uploaderName}" /></td>
   											<td><form method="post" action="${applicationScope.constants.modelDownload}?modelId=${m.id}&${_csrf.parameterName}=${_csrf.token}">
   													<button class="btn btn-action" type="submit">Download</button>
												</form>
											</td> 											
   										</tr>
   									</c:forEach>
   								</tbody>
   							</table>
						</div>
					</div>
				</div>
			</article>
		</div>				
		
	</div>
	<!-- /container -->

	<%@include file="_footer.jsp"%>
	<script src="/assets/js/validator.min.js"></script>
	<script>
		$('#myform').validator({delay:10000});
		$(document).on("focusin", "#fileText", function(event) {
			$(this).prop('readonly', true);
		});
		
		$(document).on("focusout", "#fileText", function(event) {
			$(this).prop('readonly', false);
		});
		
		$(document).on('change', '.btn-file :file', function() {
		  	var input = $(this);
			var numFiles = input.get(0).files ? input.get(0).files.length : 1;
			var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
			input.trigger('fileselect', [numFiles, label]);
		});

		$(document).ready( function() {
		    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
		        var input = $(this).parents('.input-group').find(':text');
		        var log = numFiles > 1 ? numFiles + ' files selected' : label;		        
		        if( input.length ) {
		            input.val(log);
		        } else {
		            if( log ) alert(log);
		        }		        
		    });
		});
	</script>
</body>
</html>