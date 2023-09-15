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
							<h3 class="thin text-center">Change password</h3>
							<hr>

							<form id="myform" method="post" data-toggle="validator" role="form" action="${applicationScope.constants.userChangePassword}">
								<div class="form-group">
									<label for="oldpassword" class="control-label">Old password <span class="text-danger">*</span></label>
									<input name="oldpassword" id="oldpassword" type="password" class="form-control" required>
									<div class="help-block with-errors"></div>
								</div>
								<div class="form-group">
									<label for="password" class="control-label">New password <span class="text-danger">*</span></label>
									<input name="password" id="password" type="password" class="form-control" required>
									<div class="help-block with-errors"></div>
								</div>
								<div class="form-group">
									<label for="passwordconfirm" class="control-label">Confirm password <span class="text-danger">*</span></label>
									<input name="passwordconfirm" type="password" class="form-control" data-match="#password" data-match-error="The passwords do not match." required>
									<div class="help-block with-errors"></div>
								</div>

								<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
								<hr>
								<div class="form-group">
									<button class="btn btn-action" type="submit">Change password</button>
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