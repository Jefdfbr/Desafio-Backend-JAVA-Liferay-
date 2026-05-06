<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.liferay.portal.kernel.servlet.SessionErrors" %>
<%@ include file="/init.jsp" %>

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

    <% if (SessionErrors.contains(renderRequest, "registration-failed")) { %>
    <div class="todo-alert todo-alert-error">Falha no cadastro. Tente novamente ou entre em contato com o suporte.</div>
    <% } %>
    <% if (SessionErrors.contains(renderRequest, "registration-fields-required")) { %>
    <div class="todo-alert todo-alert-error">Todos os campos são obrigatórios.</div>
    <% } %>
    <% if (SessionErrors.contains(renderRequest, "email-already-used")) { %>
    <div class="todo-alert todo-alert-error">Este e-mail já está cadastrado. Faça login para continuar.</div>
    <% } %>
    <% if (SessionErrors.contains(renderRequest, "password-too-weak")) { %>
    <div class="todo-alert todo-alert-error">A senha não atende aos requisitos mínimos de segurança (mínimo 6 caracteres).</div>
    <% } %>
    <% if (SessionErrors.contains(renderRequest, "passwords-do-not-match")) { %>
    <div class="todo-alert todo-alert-error">As senhas não coincidem.</div>
    <% } %>

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
