package com.cloudcomp.ccoms.orgsvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cloudcomp.ccoms.orgsvc.repository.OrganizationRepository;

@Controller
@RequestMapping(value = { "/", "/organization" })
public class OrganizationViewController {

    @Autowired
    OrganizationRestController orgRC;

    @GetMapping("/pretty")
    public String showSignUpForm(Model model) {
        model.addAttribute("orgs", orgRC.getDeptsEmpsAndOrgsInfo());
        return "show_pretty_output";
    }

}
