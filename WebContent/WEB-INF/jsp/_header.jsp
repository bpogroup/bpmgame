  <div id="preloader"></div>

  <header class="navbar-fixed-top">
    <!-- header-area start -->
    <div id="sticker" class="header-area">
      <div class="container">
        <div class="row">
          <div class="col-md-12 col-sm-12">

            <!-- Navigation -->
            <nav class="navbar navbar-default">
              <!-- Brand and toggle get grouped for better mobile display -->
              <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".bs-example-navbar-collapse-1" aria-expanded="false">
										<span class="sr-only">Toggle navigation</span>
										<span class="icon-bar"></span>
										<span class="icon-bar"></span>
										<span class="icon-bar"></span>
									</button>
                <!-- Brand -->
                <a class="navbar-brand page-scroll sticky-logo" href="index.html">
                  <h1><span>bpm</span>Game</h1>
                  <!-- Uncomment below if you prefer to use an image logo -->
                  <!-- <img src="img/logo.png" alt="" title=""> -->
								</a>
              </div>
              <!-- Collect the nav links, forms, and other content for toggling -->
              <div class="collapse navbar-collapse main-menu bs-example-navbar-collapse-1" id="navbar-example">
                <ul class="nav navbar-nav navbar-right">
                  <li class="active">
                    <a class="page-scroll" href="/">Home</a>
                  </li>
				<c:choose>
					<c:when test="${pageContext.request.userPrincipal.name != null}">
					  <!-- 
	                  <li>
    	                <a class="page-scroll" href="/assets/ext/manual.pdf">Manual</a>
        	          </li>
        	          -->
	                  <li>
    	                <a class="page-scroll" href="${applicationScope.constants.modelUpload}">Models</a>
        	          </li>
	                  <li>
    	                <a class="page-scroll" href="${applicationScope.constants.modelLogDownload}">Log</a>
        	          </li>
	                  <li>
    	                <a class="page-scroll" href="${applicationScope.constants.modelStats}">Stats</a>
        	          </li>
	                  <li>
    	                <a class="page-scroll" href="${applicationScope.constants.modelCases}">Cases</a>
        	          </li>
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">MY ACCOUNT <b class="caret"></b></a>
							<ul class="dropdown-menu">
								<li><a href="${applicationScope.constants.userChangePassword}">Change password</a></li>
								<li><a href="${applicationScope.constants.groupManage}">Manage group</a></li>
								<li><a href="javascript:formSubmit()">Sign out</a></li>
							</ul>
						</li>
						<c:url value="/j_spring_security_logout" var="logoutUrl" />
						<form action="${logoutUrl}" method="post" id="logoutForm">
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
						</form>
					</c:when>
					<c:otherwise>
						<li><a href="${applicationScope.constants.userRegister}">Register</a></li>
						<li><a href="${applicationScope.constants.userSignin}">Sign in</a></li>
					</c:otherwise>
				</c:choose>
                </ul>
              </div>
              <!-- navbar-collapse -->
            </nav>
            <!-- END: Navigation -->
          </div>
        </div>
      </div>
    </div>
    <!-- header-area end -->
  </header>
  <!-- header end -->
  <script>
	function formSubmit() {
		document.getElementById("logoutForm").submit();
	}
</script>
  