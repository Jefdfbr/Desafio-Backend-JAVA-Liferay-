<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/init.jsp" %>
<% String regError = ParamUtil.getString(renderRequest, "regError"); %>

<div class="todo-container">

    <% if (themeDisplay.isSignedIn()) { %>
    <div class="todo-card todo-center">
        <div class="todo-logo">&#10003;</div>
        <h3>Você já está logado!</h3>
        <portlet:renderURL var="homeURL" />
        <a href="<%= homeURL %>" class="btn-todo btn-primary">Ir para minhas tarefas</a>
    </div>
    <% } else if ("true".equals(ParamUtil.getString(request, "registrationSuccess"))) { %>

    <div class="todo-card todo-center">
        <div class="todo-logo todo-logo-success">&#10003;</div>
        <h3>Conta criada com sucesso!</h3>
        <p class="todo-subtitle">Seu cadastro foi realizado. Faça login para começar.</p>
        <a href="<%= themeDisplay.getURLSignIn() %>" class="btn-todo btn-primary btn-full">Ir para o login</a>
    </div>

    <% } else { %>

    <liferay-ui:success key="registrationSuccess" message="Conta criada com sucesso! Seu cadastro foi realizado. Faça login para começar." />
    <liferay-ui:error key="registration-failed" message="Falha no cadastro. Tente novamente ou entre em contato com o suporte." />
    <liferay-ui:error key="registration-fields-required" message="Todos os campos são obrigatórios." />
    <liferay-ui:error key="email-already-used" message="Este e-mail já está cadastrado. Faça login para continuar." />
    <liferay-ui:error key="password-too-weak" message="A senha não atende aos requisitos mínimos de segurança (mínimo 6 caracteres)." />
    <liferay-ui:error key="passwords-do-not-match" message="As senhas não coincidem." />
    <liferay-ui:error key="already-signed-in" message="Você já está logado!" />

    <div class="todo-card">
        <div class="todo-logo">&#10003;</div>
        <h2>Criar Conta</h2>
        <p class="todo-subtitle">Crie sua conta para gerenciar suas tarefas.</p>

        <portlet:actionURL name="registerUser" var="registerURL" />
        <aui:form action="<%= registerURL %>" method="post" name="registerForm">

            <div class="form-row-double">
                <aui:input name="firstName" label="Nome" required="true" maxlength="75">
                    <aui:validator name="required" />
                </aui:input>
                <aui:input name="lastName" label="Sobrenome" required="true" maxlength="75">
                    <aui:validator name="required" />
                </aui:input>
            </div>

            <aui:input name="emailAddress" label="E-mail" required="true" type="email" maxlength="255">
                <aui:validator name="required" />
                <aui:validator name="email" />
            </aui:input>

            <aui:input name="password1" label="Senha" required="true" type="password" maxlength="100">
                <aui:validator name="required" />
                <aui:validator name="minLength">6</aui:validator>
            </aui:input>

            <aui:input name="password2" label="Confirmar Senha" required="true" type="password" maxlength="100">
                <aui:validator name="required" />
                <aui:validator name="equalTo">'#<portlet:namespace />password1'</aui:validator>
            </aui:input>

            <aui:button type="submit" value="Criar Conta" cssClass="btn-todo btn-primary btn-full" />
        </aui:form>

        <div class="todo-login-link">
            <p>Já tem uma conta?
                <a href="<%= themeDisplay.getURLSignIn() %>">Entrar</a>
            </p>
        </div>
    </div>

    <% } %>
</div>
