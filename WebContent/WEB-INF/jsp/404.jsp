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
			<div class="col-sm-3"></div>
			<div class="col-sm-6">
				<div class="alert alert-danger" role="alert">
					The selected content does not exist.
				</div>
			</div>
		</div>
		
	</div> <!-- container -->

	<%@include file="_footer.jsp" %>		
</body>
</html>