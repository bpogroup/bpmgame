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
							<h3 class="thin text-center">Sign in</h3>
							<hr>
							
							<c:if test="${not empty failure}">
								<div class="alert alert-danger" role="alert">${failure}</div>
							</c:if>

							<form id="myform" method="post" data-toggle="validator" role="form" action="<c:url value='j_spring_security_check' />">
								<div class="form-group">
									<label for="email" class="control-label">E-mail <span class="text-danger">*</span></label>
									<input name="email" type="email" class="form-control" data-error="Invalid e-mail." required>
									<div class="help-block with-errors"></div>
								</div>
								<div class="form-group">
									<label for="password" class="control-label">Password <span class="text-danger">*</span></label>
									<input name="password" id="password" type="password" class="form-control" required>
									<div class="help-block with-errors"></div>
								</div>

								<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
								<hr>

								<div class="row">
									<div class="col-lg-8">
										<b><a href="${applicationScope.constants.userPassword}">Password forgotten?</a></b>
									</div>
									<div class="col-lg-4 text-right">
										<button class="btn btn-action" type="submit">Log in</button>
									</div>
								</div>
								
								<hr>
								
								<p>If you are not registered, you can do that <a href="${applicationScope.constants.userRegister}">here</a>.</p>
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