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
							<h3 class="thin text-center">Group management</h3>
							<hr>
							
							<c:if test="${not empty failure}">
								<div class="alert alert-danger" role="alert">${failure}</div>
							</c:if>

							<c:if test="${empty groupId}">					
								<form id="myform" method="post" data-toggle="validator" role="form" action="${applicationScope.constants.groupCreate}">
									<div class="form-group">
										<label for="groupName" class="control-label">Group name <span class="text-danger">*</span></label>
										<input name="groupName" type="text" class="form-control" required>
										<div class="help-block with-errors"></div>
									</div>
									
									<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

									<div class="form-group">
										<button class="btn btn-action" type="submit">Create group</button>
									</div>
								</form>
							</c:if>

							<c:if test="${not empty groupId}">
								
								<table class="table table-striped">
									<thead>
										<tr>
											<th>${groupName}</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${groupMembers}" var="member">
											<tr>
   												<td><c:out value="${member}" /></td>								
					   						</tr>
   										</c:forEach>
   									</tbody>
					   			</table>
					   										
								<form id="myform" method="post" data-toggle="validator" role="form" action="${applicationScope.constants.groupAdd}">
   									<div class="input-group">
										<input name="email" type="email" class="form-control" placeholder="E-mail address" data-error="Invalid e-mail." required>
										<div class="input-group-btn">
											<button class="btn btn-action" type="submit">Add new group member</button>
										</div>
									</div>
									
									<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
								</form>
								
								<hr>
								
								<div class="form-group">
									<button class="btn btn-action" data-href="${applicationScope.constants.groupLeave}" data-toggle="modal" data-target="#confirmleave">
										Leave group
									</button>
								</div>
								
							</c:if>
							
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
		$(document).on('show.bs.modal','#confirmleave', function (e) {
			$(this).find('.btn-ok').attr('href', $(e.relatedTarget).data('href'));
		});
	</script>

	<div class="modal fade" id="confirmleave" tabindex="-1" role="dialog" aria-labelledby="modalConfirmLeave" aria-hidden="true">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">Leaving a group
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button></h5>
				</div>
				<div class="modal-body">
					<p>Are you sure you want to leave your group? (If you leave, you will not have access to group data anymore and you can only join again if someone invites you.)</p>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-primary" data-dismiss="modal">No</button>
					<a class="btn btn-secondary btn-ok">Yes</a>
				</div>
			</div>
		</div>
	</div>
</body>
</html>