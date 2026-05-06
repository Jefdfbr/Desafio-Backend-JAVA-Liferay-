<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/init.jsp" %>

<div class="todo-container">

    <% if (themeDisplay.isSignedIn()) { %>
    <div class="todo-card todo-center">
        <div class="todo-logo">&#10003;</div>
        <h3>Você já está logado!</h3>
        <portlet:renderURL var="backURL" />
        <a href="<%= backURL %>" class="btn-todo btn-primary">Ir para minhas tarefas</a>
    </div>
    <% } else { %>

    <liferay-ui:success key="user-registered"            message="user-registered" />
    <liferay-ui:error   key="registration-failed"         message="registration-failed" />
    <liferay-ui:error   key="registration-fields-required" message="registration-fields-required" />

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
