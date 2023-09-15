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
			
			<!-- Article main content -->
			<article class="col-xs-12 maincontent">
							
				<div class="col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2">
					<div class="panel panel-default">
						<div class="panel-body">
							<h3 class="thin text-center">Password forgotten</h3>
							<p>Enter your e-mail address here and we will send you a new password.</p>
							<hr>

							<form id="myform" method="post" data-toggle="validator" role="form" action="${applicationScope.constants.userPassword}">
								<div class="form-group">
									<label for="email" class="control-label">E-mail <span class="text-danger">*</span></label>
									<input name="email" type="email" class="form-control" data-error="Invalid e-mail." required>
									<div class="help-block with-errors"></div>
								</div>

								<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
								<hr>

								<div class="form-group">
									<button class="btn btn-action" type="submit">Request new password</button>
								</div>
								
							</form>
						</div>
					</div>

				</div>
				
			</article>
			<!-- /Article -->

		</div>
	</div>	<!-- /container -->	
		
	<%@include file="_footer.jsp" %>
	<script src="/assets/js/validator.min.js"></script>
	<script>
		$('#myform').validator({delay:10000});
	</script>		
</body>
</html>